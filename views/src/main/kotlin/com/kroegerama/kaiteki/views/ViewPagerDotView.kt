package com.kroegerama.kaiteki.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withTranslation
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.kroegerama.kaiteki.dpToPxF
import java.lang.ref.WeakReference

class ViewPagerDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pInactive = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.LTGRAY
    }
    private val pActive = Paint(pInactive).apply {
        color = Color.DKGRAY
    }

    var dotColorInactive by pInactive::color
    var dotColorActive by pActive::color

    var dotSize: Float = 10.dpToPxF()
    var dotGap: Float = 8.dpToPxF()

    private var dotCount = 5
    private var dotPosition = 2
    private var dotOffset = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.ViewPagerDotView, defStyleAttr) {
            dotColorInactive = getColor(R.styleable.ViewPagerDotView_dotColorInactive, dotColorInactive)
            dotColorActive = getColor(R.styleable.ViewPagerDotView_dotColorActive, dotColorActive)
            dotSize = getDimension(R.styleable.ViewPagerDotView_dotSize, dotSize)
            dotGap = getDimension(R.styleable.ViewPagerDotView_dotGap, dotGap)
        }
    }

    fun attachToViewPager(viewPager: ViewPager2) {
        viewPager.registerOnPageChangeCallback(PageChangeCallback(viewPager))
    }

    override fun onDraw(canvas: Canvas) {
        val dotCount = dotCount
        if (dotCount < 1) return

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        val allDotsWidth = dotCount * dotSize + (dotCount - 1) * dotGap

        val r = dotSize / 2f

        canvas.withTranslation(
            x = paddingLeft.toFloat() + (contentWidth - allDotsWidth) / 2f + r, y = paddingTop.toFloat()
        ) {
            val y = contentHeight / 2f
            repeat(dotCount) { d ->
                val x = d * (dotSize + dotGap)
                drawCircle(x, y, r, pInactive)
            }

            val activeX = ((dotPosition + dotOffset) * (dotSize + dotGap)).coerceAtLeast(0f)
            drawCircle(activeX, y, r, pActive)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val preferredWidth = paddingLeft + paddingRight + (dotCount * dotSize + (dotCount - 1) * dotGap).toInt()
        val preferredHeight = paddingTop + paddingBottom + dotSize.toInt()

        var newWidth = measuredWidth
        var newHeight = measuredHeight

        var setWidth = false
        var setHeight = false
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            if (preferredWidth > 0 && newWidth != preferredWidth) {
                newWidth = preferredWidth
            }
            setWidth = true
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            if (preferredHeight > 0 && newHeight != preferredHeight) {
                newHeight = preferredHeight
            }
            setHeight = true
        }
        if (setWidth || setHeight) {
            setMeasuredDimension(if (setWidth) newWidth else measuredWidth, if (setHeight) newHeight else measuredHeight)
        }
    }

    private inner class PageChangeCallback(viewPager: ViewPager2) : OnPageChangeCallback() {
        private val weakPager = WeakReference(viewPager)
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            dotCount = weakPager.get()?.adapter?.itemCount ?: 0
            dotPosition = position
            dotOffset = positionOffset
            invalidate()
        }
    }
}
