package com.kroegerama.kaiteki.retrofit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PageKeyedDataSource
import androidx.paging.PagedList
import kotlinx.coroutines.*
import retrofit2.Response

private typealias PageApiListFun<T> = suspend (page: Int, size: Int) -> Response<List<T>>

fun <T> CoroutineScope.retrofitPageKeyedListing(
    config: PagedList.Config = DefaultPageConfig,
    pageProvider: PageProvider = DefaultPageProvider,
    apiFun: PageApiListFun<T>
): PagedListing<T> {
    val parentJob = SupervisorJob()
    val factory = RetrofitPageKeyedDataSourceFactory(this, parentJob, apiFun, pageProvider)
    val livePagedList = LivePagedListBuilder(factory, config)
        .setFetchExecutor { CoroutineScope(Dispatchers.IO).launch { it.run() } }
        .build()

    return PagedListing(
        pagedList = livePagedList,
        initialState = factory.source.switchMap { it.initialState },
        loadState = factory.source.switchMap { it.loadState },
        initialResponse = factory.source.switchMap { it.initialResponse },
        loadResponse = factory.source.switchMap { it.loadResponse },
        refreshFun = { factory.source.value?.invalidate() },
        cancelFun = { parentJob.cancel() }
    )
}

class RetrofitPageKeyedDataSourceFactory<T>(
    private val scope: CoroutineScope,
    private val parentJob: Job,
    private val apiFun: PageApiListFun<T>,
    private val pageProvider: PageProvider
) : DataSource.Factory<Int, T>() {

    val source = MutableLiveData<RetrofitPageKeyedDataSource<T>>()

    override fun create(): RetrofitPageKeyedDataSource<T> {
        val source = RetrofitPageKeyedDataSource(scope, parentJob, apiFun, pageProvider)
        this.source.postValue(source)
        return source
    }
}

class RetrofitPageKeyedDataSource<T>(
    private val scope: CoroutineScope,
    private val parentJob: Job,
    private val apiFun: PageApiListFun<T>,
    private val pageProvider: PageProvider
) : PageKeyedDataSource<Int, T>() {

    val initialState = MutableLiveData<ListingState>()
    val loadState = MutableLiveData<ListingState>()

    val initialResponse = MutableLiveData<RetryableRetrofitResponse<List<T>>>()
    val loadResponse = MutableLiveData<RetryableRetrofitResponse<List<T>>>()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, T>) {
        val page = pageProvider.firstPage
        val size = params.requestedLoadSize
        makeLoadRequest(true, page, size) { data ->
            callback.onResult(data, pageProvider.getPreviousPage(page, data.size, size), pageProvider.getNextPage(page, data.size, size))
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        val page = params.key
        val size = params.requestedLoadSize
        makeLoadRequest(false, page, size) { data ->
            callback.onResult(data, pageProvider.getNextPage(page, data.size, size))
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, T>) {
        val page = params.key
        val size = params.requestedLoadSize
        makeLoadRequest(false, page, size) { data ->
            callback.onResult(data, pageProvider.getPreviousPage(page, data.size, size))
        }
    }

    private fun makeLoadRequest(
        isInitial: Boolean,
        currentPage: Int,
        size: Int,
        callback: (List<T>) -> Any
    ): Job = scope.launch(parentJob) {
        updateState(isInitial, ListingState.RUNNING)
        val result = retrofitCall { apiFun(currentPage, size) }

        if (result is RetrofitResponse.Success) {
            val items = result.data.orEmpty()
            callback(items)
            updateRetry(isInitial, result, null)
            updateState(isInitial, ListingState.FINISHED)
        } else {
            updateRetry(isInitial, result) {
                makeLoadRequest(isInitial, currentPage, size, callback)
            }
            updateState(isInitial, ListingState.IDLE)
        }
    }

    private suspend fun updateRetry(isInitial: Boolean, response: RetrofitResponse<List<T>>, retry: RetryFun?) = withContext(Dispatchers.Main) {
        if (isInitial) {
            initialResponse.value = RetryableRetrofitResponse(response, retry)
        } else {
            loadResponse.value = RetryableRetrofitResponse(response, retry)
        }
    }

    private suspend fun updateState(isInitial: Boolean, state: ListingState) = withContext(Dispatchers.Main) {
        if (isInitial) {
            initialState.value = state
        }
        loadState.value = state
    }
}