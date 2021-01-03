package com.kroegerama.kaiteki.retrofit.pagination

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.kroegerama.kaiteki.retrofit.ListingState
import com.kroegerama.kaiteki.retrofit.RetryableRetrofitResource
import java.io.Closeable

internal typealias RetryFun = () -> Unit

interface PageProvider {
    val firstPage: Int
    fun getNextPage(currentPage: Int, currentSize: Int, requestedSize: Int): Int?
    fun getPreviousPage(currentPage: Int, currentSize: Int, requestedSize: Int): Int?
}

object DefaultPageProvider : PageProvider {
    override val firstPage = 0

    override fun getNextPage(currentPage: Int, currentSize: Int, requestedSize: Int) =
        if (currentSize < requestedSize) null else currentPage + 1

    override fun getPreviousPage(currentPage: Int, currentSize: Int, requestedSize: Int): Int? = null
}

val DefaultPageConfig by lazy { PagedList.Config.Builder().setPageSize(10).setPrefetchDistance(20).build() }

class PagedListing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val initialState: LiveData<ListingState>,
    val loadState: LiveData<ListingState>,
    val initialResponse: LiveData<RetryableRetrofitResource<List<T>>>,
    val loadResponse: LiveData<RetryableRetrofitResource<List<T>>>,
    private val refreshFun: () -> Unit,
    private val cancelFun: () -> Unit
) : Closeable {
    override fun close() = cancel()

    fun refresh() = refreshFun.invoke()
    fun cancel() = cancelFun.invoke()

    fun retryInitial() {
        initialResponse.value?.retry()
    }

    fun retryLoad() {
        loadResponse.value?.retry()
    }
}

fun <T> LiveData<out PagedListing<T>>.pagedList() = switchMap { it.pagedList }
fun <T> LiveData<out PagedListing<T>>.initialRunning() = switchMap { it.initialState }.map { it.isRunning }
fun <T> LiveData<out PagedListing<T>>.loadRunning() = switchMap { it.loadState }.map { it.isRunning }
fun <T> LiveData<out PagedListing<T>>.initialResult() = switchMap { it.initialResponse }
fun <T> LiveData<out PagedListing<T>>.loadResult() = switchMap { it.loadResponse }
fun LiveData<out PagedListing<*>>.refresh() {
    value?.refresh()
}

fun LiveData<out PagedListing<*>>.retryInitial() {
    value?.retryInitial()
}

fun LiveData<out PagedListing<*>>.retryLoad() {
    value?.retryLoad()
}

fun LiveData<out PagedListing<*>>.cancel() {
    value?.cancel()
}