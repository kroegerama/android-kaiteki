package com.kroegerama.kaiteki.recyclerview.layout

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(@Px private val spacing: Int) : RecyclerView.ItemDecoration() {

    companion object {
        fun fromResource(context: Context, @DimenRes resId: Int): SpaceItemDecoration {
            return SpaceItemDecoration(context.resources.getDimensionPixelSize(resId))
        }
    }

    private var orientation = -1
    private var colCount = -1

    private fun getOrientation(parent: RecyclerView): Int {
        if (orientation == -1) {
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager = parent.layoutManager as LinearLayoutManager
                orientation = layoutManager.orientation
            } else {
                throw IllegalStateException(
                        "SpaceItemDecoration can only be used with a LinearLayoutManager.")
            }
        }
        return orientation
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (spacing == 0) {
            return
        }

        if (orientation == -1) {
            getOrientation(parent)
        }

        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        if (position == 0) {
            colCount = 1
            if (parent.layoutManager is GridLayoutManager) {
                colCount = (parent.layoutManager as GridLayoutManager).spanCount
            }
        }


        if (orientation == LinearLayoutManager.VERTICAL) {
            if (position % colCount == 0) {
                outRect.left = spacing
            } else {
                outRect.left = spacing / 2
            }
            if (position % colCount == colCount - 1) {
                outRect.right = spacing
            } else {
                outRect.right = spacing / 2
            }

            if (position < colCount) {
                outRect.top = spacing
            } else {
                outRect.top = spacing / 2
            }
            if (position > state.itemCount - colCount - 1) {
                if (state.itemCount % colCount != 0 && position < state.itemCount - state.itemCount % colCount) {
                    outRect.bottom = spacing / 2
                } else {
                    outRect.bottom = spacing
                }
            } else {
                outRect.bottom = spacing / 2
            }
        } else {
            outRect.left = spacing
            outRect.top = spacing
        }
    }
}