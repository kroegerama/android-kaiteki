package com.kroegerama.kaiteki.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

const val DEFAULT_PAGE_SIZE = 20

fun <Key : Any, Value : Any> defaultPager(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    initialLoadSize: Int = DEFAULT_PAGE_SIZE,
    pagingSourceFactory: () -> PagingSource<Key, Value>
) = Pager(
    config = PagingConfig(pageSize = pageSize, initialLoadSize = initialLoadSize),
    pagingSourceFactory = pagingSourceFactory
)

fun <Key : Any, Value : Any> ViewModel.pager(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    initialLoadSize: Int = DEFAULT_PAGE_SIZE,
    pagingSourceFactory: () -> PagingSource<Key, Value>
): PagerInfo<Value> {
    val result = PagerInfo<Value>()
    result.flow = defaultPager(
        pageSize = pageSize,
        initialLoadSize = initialLoadSize
    ) {
        pagingSourceFactory().also { result.dataSource = it }
    }.flow.cachedIn(viewModelScope)
    return result
}

val PagingDataAdapter<*, *>.isEmptyFlow
    get() = loadStateFlow.map { it.refresh }.distinctUntilChanged().map {
        it is LoadState.NotLoading && itemCount == 0
    }

fun PagingDataAdapter<*, *>.addOnEmptyListener(listener: (empty: Boolean) -> Unit) = addLoadStateListener { state ->
    listener(
        state.refresh is LoadState.NotLoading &&
                state.append.endOfPaginationReached &&
                state.prepend.endOfPaginationReached &&
                itemCount < 1
    )
}
