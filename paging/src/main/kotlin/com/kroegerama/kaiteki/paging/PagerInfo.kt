package com.kroegerama.kaiteki.paging

import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

class PagerInfo<T : Any> {
    lateinit var flow: Flow<PagingData<T>>
    var dataSource: PagingSource<*, T>? = null

    fun invalidate() {
        dataSource?.invalidate()
    }
}
