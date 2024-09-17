package com.kroegerama.kaiteki.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

const val DEFAULT_PAGE_SIZE = 20

fun <Key : Any, Value : Any> defaultPager(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    initialLoadSize: Int = DEFAULT_PAGE_SIZE,
    pagingSourceFactory: () -> PagingSource<Key, Value>
): Pager<Key, Value> = Pager(
    config = PagingConfig(pageSize = pageSize, initialLoadSize = initialLoadSize),
    pagingSourceFactory = pagingSourceFactory
)

fun <Param, Key : Any, Value : Any> defaultPager(
    parameterFlow: Flow<Param>,
    scope: CoroutineScope,
    pageSize: Int = DEFAULT_PAGE_SIZE,
    initialLoadSize: Int = DEFAULT_PAGE_SIZE,
    pagingSourceFactory: (Param) -> PagingSource<Key, Value>
): Pager<Key, Value> {
    var currentParameter: Option<Param> = None
    var currentPagingSource: PagingSource<Key, Value>? = null

    parameterFlow.onEach {
        currentParameter = it.some()
        currentPagingSource?.invalidate()
    }.launchIn(scope)

    return Pager(
        config = PagingConfig(pageSize = pageSize, initialLoadSize = initialLoadSize),
        pagingSourceFactory = {
            when (val param = currentParameter) {
                None -> EmptyPagingSource()
                is Some -> pagingSourceFactory(param.value)
            }.also {
                currentPagingSource = it
            }
        }
    )
}

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

fun <Param, Key : Any, Value : Any> ViewModel.pager(
    parameterFlow: Flow<Param>,
    pageSize: Int = DEFAULT_PAGE_SIZE,
    initialLoadSize: Int = DEFAULT_PAGE_SIZE,
    pagingSourceFactory: (Param) -> PagingSource<Key, Value>
): PagerInfo<Value> {
    val result = PagerInfo<Value>()
    result.flow = defaultPager(
        parameterFlow = parameterFlow,
        scope = viewModelScope,
        pageSize = pageSize,
        initialLoadSize = initialLoadSize
    ) { param ->
        pagingSourceFactory(param).also { result.dataSource = it }
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
