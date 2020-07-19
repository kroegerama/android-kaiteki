package com.kroegerama.kaiteki.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import java.io.Closeable

internal typealias RetryFun = () -> Unit

interface PageProvider {
    val firstPage: Int
    fun getNextPage(currentPage: Int): Int?
    fun getPreviousPage(currentPage: Int): Int?
}

object DefaultPageProvider : PageProvider {
    override val firstPage = 0
    override fun getNextPage(currentPage: Int) = currentPage + 1
    override fun getPreviousPage(currentPage: Int): Int? = null
}

val DefaultPageConfig by lazy { PagedList.Config.Builder().setPageSize(10).setPrefetchDistance(20).build() }

class PagedListing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val initialState: LiveData<ListingState>,
    val loadState: LiveData<ListingState>,
    val initialResponse: LiveData<RetryableRetrofitResponse<List<T>>>,
    val loadResponse: LiveData<RetryableRetrofitResponse<List<T>>>,
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

fun <T> LiveData<PagedListing<T>>.pagedList() = switchMap { it.pagedList }
fun <T> LiveData<PagedListing<T>>.initialRunning() = switchMap { it.initialState }.map { it.isRunning }
fun <T> LiveData<PagedListing<T>>.loadRunning() = switchMap { it.loadState }.map { it.isRunning }
fun <T> LiveData<PagedListing<T>>.initialResult() = switchMap { it.initialResponse }
fun <T> LiveData<PagedListing<T>>.loadResult() = switchMap { it.loadResponse }
fun LiveData<PagedListing<*>>.refresh() = value?.refresh()
fun LiveData<PagedListing<*>>.retryInitial() = value?.retryInitial()
fun LiveData<PagedListing<*>>.retryLoad() = value?.retryLoad()
fun LiveData<PagedListing<*>>.cancel() = value?.cancel()