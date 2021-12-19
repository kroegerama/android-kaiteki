package com.kroegerama.kaiteki.recyclerview

import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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
