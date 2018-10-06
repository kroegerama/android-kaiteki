package com.kroegerama.kaiteki.recyclerview.layout

import android.graphics.Rect
import android.support.annotation.Px
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

class StaggeredGridLayoutSpaceItemDecoration(
        @Px private val spacing: Int
) : RecyclerView.ItemDecoration() {

    private val spacing_h = spacing / 2

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager as? StaggeredGridLayoutManager
                ?: throw IllegalStateException("only StaggeredGridLayoutManager supported")

        val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams

        val count = parent.adapter?.itemCount ?: 0
        val pos = lp.viewLayoutPosition

        val spanCount = layoutManager.spanCount
        val spanIndex = lp.spanIndex
        val fullSpan = lp.isFullSpan

        if (layoutManager.orientation == StaggeredGridLayoutManager.HORIZONTAL) {
            outRect.left = if (spanIndex == 0) spacing else spacing_h
            outRect.top = if (pos < spanCount) spacing else spacing_h
            outRect.right = if ((spanIndex == spanCount - 1) || fullSpan) spacing else spacing_h
            outRect.bottom = if (pos >= (count - spanCount)) spacing else spacing_h
        } else {
            outRect.left = if (pos < spanCount) spacing else spacing_h
            outRect.top = if (spanIndex == 0) spacing else spacing_h
            outRect.right = if (pos >= (count - spanCount)) spacing else spacing_h
            outRect.bottom = if ((spanIndex == spanCount - 1) || fullSpan) spacing else spacing_h
        }
    }
}