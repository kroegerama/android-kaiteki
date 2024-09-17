package com.kroegerama.kaiteki.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class EmptyPagingSource<Key : Any, Value : Any> : PagingSource<Key, Value>() {
    override fun getRefreshKey(state: PagingState<Key, Value>): Key? = null
    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> = LoadResult.Page(
        data = emptyList(),
        prevKey = null,
        nextKey = null
    )
}
