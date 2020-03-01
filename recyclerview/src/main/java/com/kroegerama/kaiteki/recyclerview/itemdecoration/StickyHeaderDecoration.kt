package com.kroegerama.kaiteki.recyclerview.itemdecoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.RecyclerView


class StickyHeaderDecoration<VH : RecyclerView.ViewHolder>(
    private val headerProvider: HeaderProvider<VH>
) : RecyclerView.ItemDecoration() {

    private var stickyHeaderHeight = 0
    private var currentHeader: Pair<Int, RecyclerView.ViewHolder>? = null

    private var headerRef: View? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPos = headerProvider.getHeaderPositionForItem(topChildPosition)
        val currentHeader = if (headerProvider.isHeader(topChildPosition)) {
            topChild
        } else {
            getHeaderViewForItem(headerPos, parent)
        }

        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPos)

        headerRef?.let { ViewCompat.setClipBounds(it, null) }
        if (childInContact != null && headerProvider.isHeader(
                parent.getChildAdapterPosition(
                    childInContact
                )
            )
        ) {
            drawHeaderTranslated(c, currentHeader, childInContact)
            return
        }

        if (headerProvider.isHeader(topChildPosition)) {
            headerRef = topChild.also { ViewCompat.setClipBounds(it, EMPTY_RECT) }
        }
        drawHeader(c, currentHeader)
    }

    private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View {
        currentHeader?.run { if (first == headerPosition) return second.itemView }
        val vh = headerProvider.onCreateHeaderViewHolder(headerPosition, parent)
        headerProvider.onBindHeaderViewHolder(vh, headerPosition)
        currentHeader = headerPosition to vh
        return vh.itemView.also { fixLayoutSize(parent, it) }
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.withSave {
            header.draw(this)
        }
    }

    private fun drawHeaderTranslated(c: Canvas, currentHeader: View, nextHeader: View) {
        c.withTranslation(0f, (nextHeader.top - currentHeader.height).toFloat()) {
            currentHeader.draw(this)
        }
    }

    private fun getChildInContact(
        parent: RecyclerView,
        contactPoint: Int,
        currentHeaderPos: Int
    ): View? {
        parent.forEachIndexed { index, child ->
            var heightTolerance = 0

            //measure height tolerance with child if child is another header
            if (currentHeaderPos != index) {
                val childPos = parent.getChildAdapterPosition(child)
                if (headerProvider.isHeader(childPos)) {
                    heightTolerance = stickyHeaderHeight - child.height
                }
            }

            //add heightTolerance if child top be in display area
            val childBottomPosition = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }
            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    return child
                }
            }
        }
        return null
    }

    /**
     * Properly measures and layouts the top sticky header.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec =
            ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                view.layoutParams.width
            )
        val childHeightSpec =
            ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height
            )

        view.measure(childWidthSpec, childHeightSpec)

        stickyHeaderHeight = view.measuredHeight
        view.layout(0, 0, view.measuredWidth, stickyHeaderHeight)
    }

    interface HeaderProvider<VH : RecyclerView.ViewHolder> {
        fun getHeaderPositionForItem(position: Int): Int

        fun onCreateHeaderViewHolder(position: Int, parent: ViewGroup): VH

        fun onBindHeaderViewHolder(header: VH, position: Int)

        fun isHeader(position: Int): Boolean
    }

    companion object {
        private const val TAG = "StickyHeaderDecoration"

        private val EMPTY_RECT by lazy { Rect(0, 0, 0, 0) }
    }
}