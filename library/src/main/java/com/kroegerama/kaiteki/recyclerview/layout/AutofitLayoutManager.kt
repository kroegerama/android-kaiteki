package com.kroegerama.kaiteki.recyclerview.layout

import android.content.Context
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kroegerama.kaiteki.dpToPx

class AutofitLayoutManager(
        context: Context,
        @DimenRes colWidthRes: Int,
        @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
        reverseLayout: Boolean = false
) : GridLayoutManager(context, 1, orientation, reverseLayout) {

    private var colWidth: Int = 0
        set(value) {
            if (value > 0 && value != field) {
                field = value
                colWidthChanged = true
            }
        }

    init {
        val width = context.resources.getDimensionPixelSize(colWidthRes)
        colWidth = if (width <= 0) 48F.dpToPx() else width
    }

    private var colWidthChanged: Boolean = false

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        if (colWidthChanged && colWidth > 0) {
            val totalSpace: Int = if (orientation == LinearLayoutManager.VERTICAL) {
                width - paddingRight - paddingLeft
            } else {
                height - paddingTop - paddingBottom
            }
            val spanCount = Math.max(1, totalSpace / colWidth)
            setSpanCount(spanCount)
            colWidthChanged = false
        }
        super.onLayoutChildren(recycler, state)
    }
}