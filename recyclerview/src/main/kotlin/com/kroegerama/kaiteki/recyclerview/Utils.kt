package com.kroegerama.kaiteki.recyclerview

import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
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

fun RecyclerView.addDividers(isLastItemDecorated: Boolean = true) = addItemDecoration(
    MaterialDividerItemDecoration(
        context,
        MaterialDividerItemDecoration.VERTICAL
    ).also {
        it.isLastItemDecorated = isLastItemDecorated
    }
)
