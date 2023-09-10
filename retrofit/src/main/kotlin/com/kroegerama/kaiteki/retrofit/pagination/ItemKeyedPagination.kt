@file:Suppress("DEPRECATION")

package com.kroegerama.kaiteki.retrofit.pagination

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.kroegerama.kaiteki.retrofit.ListingState
import com.kroegerama.kaiteki.retrofit.RetrofitResource
import com.kroegerama.kaiteki.retrofit.RetryableRetrofitResource
import com.kroegerama.kaiteki.retrofit.retrofitCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

private typealias ItemApiListFun<Key, T> = suspend (key: Key?, size: Int) -> Response<List<T>>
private typealias KeyProvider<Key, T> = (T) -> Key
private typealias ShouldLoadFun<Key> = (Key) -> Boolean

@Deprecated("switch to Paging 3")
fun <Key : Any, T : Any> CoroutineScope.retrofitItemKeyedListing(
    config: PagedList.Config = DefaultPageConfig,
    keyProvider: KeyProvider<Key, T>,
    shouldLoadBefore: ShouldLoadFun<Key>,
    shouldLoadAfter: ShouldLoadFun<Key>,
    apiFun: ItemApiListFun<Key, T>
): PagedListing<T> {
    val parentJob = SupervisorJob()
    val factory = RetrofitItemKeyedDataSourceFactory(
        this,
        parentJob,
        apiFun,
        keyProvider,
        shouldLoadBefore,
        shouldLoadAfter
    )
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

@Deprecated("switch to Paging 3")
class RetrofitItemKeyedDataSourceFactory<Key : Any, T : Any>(
    private val scope: CoroutineScope,
    private val parentJob: Job,
    private val apiFun: ItemApiListFun<Key, T>,
    private val keyProvider: KeyProvider<Key, T>,
    private val shouldLoadBefore: ShouldLoadFun<Key>,
    private val shouldLoadAfter: ShouldLoadFun<Key>
) : DataSource.Factory<Key, T>() {

    val source = MutableLiveData<RetrofitItemKeyedDataSource<Key, T>>()

    override fun create(): RetrofitItemKeyedDataSource<Key, T> {
        val source = RetrofitItemKeyedDataSource(
            scope = scope,
            parentJob = parentJob,
            apiFun = apiFun,
            keyProvider = keyProvider,
            shouldLoadBefore = shouldLoadBefore,
            shouldLoadAfter = shouldLoadAfter
        )
        this.source.postValue(source)
        return source
    }
}

@Deprecated("switch to Paging 3")
class RetrofitItemKeyedDataSource<Key : Any, T : Any>(
    private val scope: CoroutineScope,
    private val parentJob: Job,
    private val apiFun: ItemApiListFun<Key, T>,
    private val keyProvider: KeyProvider<Key, T>,
    private val shouldLoadBefore: ShouldLoadFun<Key>,
    private val shouldLoadAfter: ShouldLoadFun<Key>
) : ItemKeyedDataSource<Key, T>() {

    val initialState = MutableLiveData<ListingState>()
    val loadState = MutableLiveData<ListingState>()

    val initialResponse = MutableLiveData<RetryableRetrofitResource<List<T>>>()
    val loadResponse = MutableLiveData<RetryableRetrofitResource<List<T>>>()

    override fun getKey(item: T) = keyProvider(item)

    override fun loadInitial(params: LoadInitialParams<Key>, callback: LoadInitialCallback<T>) {
        val key = params.requestedInitialKey
        val size = params.requestedLoadSize
        makeLoadRequest(key, size) { data ->
            callback.onResult(data.reversed())
        }
    }

    override fun loadAfter(params: LoadParams<Key>, callback: LoadCallback<T>) {
        val key = params.key
        val size = params.requestedLoadSize
        if (shouldLoadAfter(key)) {
            makeLoadRequest(key, size) { data ->
                callback.onResult(data)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Key>, callback: LoadCallback<T>) {
        val key = params.key
        val size = params.requestedLoadSize
        if (shouldLoadBefore(key)) {
            makeLoadRequest(key, size) { data ->
                callback.onResult(data.reversed())
            }
        }
    }

    private fun makeLoadRequest(
        key: Key?,
        size: Int,
        callback: (List<T>) -> Any
    ): Job = scope.launch(parentJob) {
        val isInitial = key == null
        updateState(isInitial, ListingState.RUNNING)
        val result = retrofitCall { apiFun(key, size) }

        if (result is RetrofitResource.Success) {
            val items = result.data.orEmpty()
            callback(items)
            updateRetry(isInitial, result, null)
            updateState(isInitial, ListingState.FINISHED)
        } else {
            updateRetry(isInitial, result) {
                makeLoadRequest(key, size, callback)
            }
            updateState(isInitial, ListingState.IDLE)
        }
    }

    private suspend fun updateRetry(isInitial: Boolean, response: RetrofitResource<List<T>>, retry: RetryFun?) = withContext(Dispatchers.Main) {
        if (isInitial) {
            initialResponse.value = RetryableRetrofitResource(response, retry)
        } else {
            loadResponse.value = RetryableRetrofitResource(response, retry)
        }
    }

    private suspend fun updateState(isInitial: Boolean, state: ListingState) = withContext(Dispatchers.Main) {
        if (isInitial) {
            initialState.value = state
        }
        loadState.value = state
    }
}