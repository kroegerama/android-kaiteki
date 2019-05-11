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
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kroegerama.kaiteki.getDimension
import com.kroegerama.kaiteki.lineHeight
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
            swipeToStartItem?.draw(this, false, itemView, dX)
            swipeToEndItem?.draw(this, true, itemView, dX)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    open class SwipeItem(
            context: Context,
            @ColorRes colorRes: Int,
            @DrawableRes iconRes: Int,
            val onSwipe: (position: Int) -> Unit
    ) {
        protected val color by lazy { ContextCompat.getColor(context, colorRes) }
        protected val icon by lazy {
            ContextCompat.getDrawable(context, iconRes)!!.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
        }

        fun draw(c: Canvas, drawLeft: Boolean, itemView: View, dX: Float) {
            if (drawLeft) {
                if (dX <= 0) {
                    return
                }
                c.withClip(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom) {
                    drawInternal(this, drawLeft, itemView, dX)
                }
            } else {
                if (dX >= 0) {
                    return
                }
                c.withClip(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom) {
                    drawInternal(this, drawLeft, itemView, dX)
                }
            }
        }

        protected open fun drawInternal(c: Canvas, drawLeft: Boolean, itemView: View, dX: Float) {
            val itemHeight = itemView.bottom - itemView.top
            c.drawColor(color)
            val iconMargin = round((itemHeight - icon.intrinsicHeight) / 2f)
            if (drawLeft) {
                c.withTranslation(
                        itemView.left + iconMargin,
                        itemView.top + iconMargin,
                        icon::draw
                )
                c.translate(itemView.left + iconMargin + icon.intrinsicWidth + iconMargin, 0f)
            } else {
                c.withTranslation(
                        itemView.right - icon.intrinsicWidth - iconMargin,
                        itemView.top + iconMargin,
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
        protected val text: String by lazy { context.getString(textRes) }

        protected val textPaint by lazy {
            TextPaint().apply {
                isAntiAlias = true
                color = ContextCompat.getColor(context, textColorRes)
                textSize = context.getDimension(textSizeRes)
                style = Paint.Style.FILL
            }
        }
        protected val textHeight by lazy { textPaint.fontMetrics.lineHeight }

        override fun drawInternal(c: Canvas, drawLeft: Boolean, itemView: View, dX: Float) {
            super.drawInternal(c, drawLeft, itemView, dX)

            textPaint.textAlign = if (drawLeft) Paint.Align.LEFT else Paint.Align.RIGHT
            val tY = itemView.top + round((itemView.height - textHeight) / 2f - textPaint.fontMetrics.ascent)
            if (drawLeft) {
                c.drawText(text, itemView.left.toFloat(), tY, textPaint)
            } else {
                c.drawText(text, itemView.right.toFloat(), tY, textPaint)
            }
        }

    }
}
