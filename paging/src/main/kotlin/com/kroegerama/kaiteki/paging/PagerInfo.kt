package com.kroegerama.kaiteki.paging

import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

class PagerInfo<T : Any> internal constructor() {
    lateinit var flow: Flow<PagingData<T>>
        internal set
    var dataSource: PagingSource<*, T>? = null
        internal set

    fun invalidate() {
        dataSource?.invalidate()
    }
}
