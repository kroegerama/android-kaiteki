package com.kroegerama.kaiteki.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.use

@SuppressLint("Recycle")
class BiasCropImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var horizontalCropBias = 0.5f
    private var verticalCropBias = 0.5f

    init {
        scaleType = ScaleType.MATRIX

        context.obtainStyledAttributes(attrs, R.styleable.BiasCropImageView).use { arr ->
            val hBias = arr.getFloat(R.styleable.BiasCropImageView_horizontalCropBias, horizontalCropBias)
            val vBias = arr.getFloat(R.styleable.BiasCropImageView_verticalCropBias, verticalCropBias)
            setCropBias(hBias, vBias)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyCropOffset()
    }

    /**
     * Sets the crop box offset by the specified percentage values. For example, a center-crop would
     * be (0.5, 0.5), a top-left crop would be (0, 0), and a bottom-center crop would be (0.5, 1)
     */
    fun setCropBias(horizontalCropBias: Float, verticalCropBias: Float) {
        this.horizontalCropBias = horizontalCropBias.coerceIn(0.0f..1.0f)
        this.verticalCropBias = verticalCropBias.coerceIn(0.0f..1.0f)
        applyCropOffset()
    }

    private fun applyCropOffset() {
        val currentDrawable = drawable ?: return

        val drawableWidth = currentDrawable.intrinsicWidth.toFloat()
        val drawableHeight = currentDrawable.intrinsicHeight.toFloat()

        if (drawableWidth == 0f || drawableHeight == 0f) return

        val contentWidth = (width - paddingLeft - paddingRight).toFloat()
        val contentHeight = (height - paddingTop - paddingBottom).toFloat()

        val scale = if (drawableWidth * contentHeight > drawableHeight * contentWidth) {
            // Drawable is flatter than view. Scale it to fill the view height.
            // A Top/Bottom crop here should be identical in this case.
            contentHeight / drawableHeight
        } else {
            // Drawable is taller than view. Scale it to fill the view width.
            // Left/Right crop here should be identical in this case.
            contentWidth / drawableWidth
        }

        val viewToDrawableWidth = contentWidth / scale
        val viewToDrawableHeight = contentHeight / scale
        val xOffset = horizontalCropBias * (drawableWidth - viewToDrawableWidth)
        val yOffset = verticalCropBias * (drawableHeight - viewToDrawableHeight)

        // Define the rect from which to take the image portion.
        val drawableRect = RectF(0f, 0f, viewToDrawableWidth, viewToDrawableHeight).apply {
            offset(xOffset, yOffset)
        }
        val viewRect = RectF(0f, 0f, contentWidth, contentHeight)

        imageMatrix = Matrix().apply {
            setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.FILL)
        }
    }

}