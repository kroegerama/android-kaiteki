package com.kroegerama.kaiteki.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.round


class KaitekiSwipeCallback(
    private val swipeToStartItem: SwipeItem? = null,
    private val swipeToEndItem: SwipeItem? = null
) : ItemTouchHelper.SimpleCallback(
    0,
    (if (swipeToStartItem != null) ItemTouchHelper.START else 0) or
            (if (swipeToEndItem != null) ItemTouchHelper.END else 0)
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.START -> swipeToStartItem?.onSwipe?.invoke(viewHolder.adapterPosition)
            ItemTouchHelper.END -> swipeToEndItem?.onSwipe?.invoke(viewHolder.adapterPosition)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        c.withSave {
            swipeToStartItem?.draw(this, false, recyclerView, itemView, dX)
            swipeToEndItem?.draw(this, true, recyclerView, itemView, dX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    open class SwipeItem(
        context: Context,
        @ColorRes colorRes: Int,
        @DrawableRes iconRes: Int,
        val onSwipe: (position: Int) -> Unit
    ) {
        protected val color = ContextCompat.getColor(context, colorRes)
        protected val icon =
            ResourcesCompat.getDrawable(context.resources, iconRes, context.theme)!!.apply {
                setBounds(
                    0,
                    0,
                    intrinsicWidth,
                    intrinsicHeight
                )
            }

        fun draw(
            c: Canvas,
            drawLeft: Boolean,
            recyclerView: RecyclerView,
            itemView: View,
            dX: Float
        ) {
            val itemWidth = itemView.right - itemView.left
            if (drawLeft) {
                if (dX <= 0) {
                    return
                }
                c.withClip(
                    recyclerView.left,
                    itemView.top,
                    itemView.right - itemWidth / 2 + dX.toInt(),
                    itemView.bottom
                ) {
                    drawInternal(this, drawLeft, recyclerView, itemView, dX)
                }
            } else {
                if (dX >= 0) {
                    return
                }
                c.withClip(
                    itemView.left + itemWidth / 2 + dX.toInt(),
                    itemView.top,
                    recyclerView.right,
                    itemView.bottom
                ) {
                    drawInternal(this, drawLeft, recyclerView, itemView, dX)
                }
            }
        }

        protected open fun drawInternal(
            c: Canvas,
            drawLeft: Boolean,
            recyclerView: RecyclerView,
            itemView: View,
            dX: Float
        ) {
            val itemHeight = itemView.bottom - itemView.top
            val iconMargin = icon.intrinsicWidth.toFloat()

            val alpha = (minOf(abs(dX) / iconMargin / 3f, 1f) * 255f).toInt()

            c.drawColor((alpha shl 24) or (color and 0xFFFFFF))
            if (drawLeft) {
                c.withTranslation(
                    itemView.left + iconMargin,
                    itemView.top + (itemHeight - icon.intrinsicHeight) / 2f,
                    icon::draw
                )
                c.translate(itemView.left + iconMargin * 2 + icon.intrinsicWidth, 0f)
            } else {
                c.withTranslation(
                    itemView.right - icon.intrinsicWidth - iconMargin,
                    itemView.top + (itemHeight - icon.intrinsicHeight) / 2f,
                    icon::draw
                )
                c.translate(-(icon.intrinsicWidth + iconMargin * 2), 0f)
            }
        }
    }

    open class TextSwipeItem(
        context: Context,
        @ColorRes colorRes: Int,
        @DrawableRes iconRes: Int,
        @StringRes textRes: Int,
        @DimenRes textSizeRes: Int,
        @ColorRes textColorRes: Int,
        onSwipe: (position: Int) -> Unit
    ) : SwipeItem(context, colorRes, iconRes, onSwipe) {
        protected val text: String = context.getString(textRes)

        protected val textPaint = TextPaint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, textColorRes)
            textSize = context.resources.getDimension(textSizeRes)
            style = Paint.Style.FILL
        }

        protected val textHeight by lazy { textPaint.fontMetrics.run { descent - ascent } }

        override fun drawInternal(
            c: Canvas,
            drawLeft: Boolean,
            recyclerView: RecyclerView,
            itemView: View,
            dX: Float
        ) {
            super.drawInternal(c, drawLeft, recyclerView, itemView, dX)

            textPaint.textAlign = if (drawLeft) Paint.Align.LEFT else Paint.Align.RIGHT
            val tY =
                itemView.top + round((itemView.height - textHeight) / 2f - textPaint.fontMetrics.ascent)
            if (drawLeft) {
                c.drawText(text, itemView.left.toFloat(), tY, textPaint)
            } else {
                c.drawText(text, itemView.right.toFloat(), tY, textPaint)
            }
        }

    }
}
