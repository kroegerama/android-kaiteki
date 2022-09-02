package com.kroegerama.kaiteki.recyclerview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.flow.Flow

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

class PagerInfo<T : Any> {
    lateinit var flow: Flow<PagingData<T>>
    var dataSource: PagingSource<*, T>? = null

    fun invalidate() {
        dataSource?.invalidate()
    }
}
