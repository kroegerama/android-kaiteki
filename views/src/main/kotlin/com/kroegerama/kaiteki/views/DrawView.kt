package com.kroegerama.kaiteki.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import kotlin.math.pow

class DrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pStroke = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 4f
        color = Color.BLACK
    }

    var drawStrokeColor
        get() = pStroke.color
        set(value) {
            pStroke.color = value
        }

    var drawStrokeWidth
        get() = pStroke.strokeWidth
        set(value) {
            pStroke.strokeWidth = value
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.DrawView, defStyleAttr) {
            drawStrokeWidth = getDimension(R.styleable.DrawView_drawStrokeWidth, drawStrokeWidth)
            drawStrokeColor = getColor(R.styleable.DrawView_drawStrokeColor, drawStrokeColor)
        }
    }

    var signature: Signature? = null
        set(value) {
            field = value
            invalidate()
        }

    private var currentPath = SignaturePath()
    private var isDown = false

    private fun setDummySignature() {
        signature = Signature().apply {
            add(SignaturePath().apply {
                repeat(101) {
                    val x = it / 100f
                    val y = 0.5f + (x * 2 - 1).pow(3) * 0.5f
                    if (it == 0) {
                        moveTo(x * width, height - y * height)
                    } else {
                        lineTo(x * width, height - y * height)
                    }
                }
            })
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isInEditMode) {
            setDummySignature()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return onDown(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return onUp(event)
            MotionEvent.ACTION_MOVE -> return onMove(event)
        }
        return super.onTouchEvent(event)
    }

    val isEmpty get() = signature?.isEmpty ?: true

    fun clear() {
        signature = null
        currentPath = SignaturePath()
        invalidate()
    }

    private fun onDown(event: MotionEvent): Boolean {
        isDown = true
        currentPath = SignaturePath().apply {
            moveTo(event.x, event.y)
        }
        parent.requestDisallowInterceptTouchEvent(true)
        invalidate()
        return true
    }

    private fun onMove(event: MotionEvent): Boolean {
        if (!isDown) return false

        currentPath.lineTo(event.x, event.y)
        invalidate()
        return true
    }

    private fun onUp(event: MotionEvent): Boolean {
        if (!isDown) return false
        isDown = false

        currentPath.lineTo(event.x, event.y)
        (signature ?: Signature().apply { signature = this }).add(currentPath)
        currentPath = SignaturePath()
        invalidate()
        return true
    }

    private fun drawPaths(canvas: Canvas, paint: Paint) {
        signature?.drawTo(canvas, paint)
        canvas.drawPath(currentPath.asPath(), paint)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withClip(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom) {
            drawPaths(this, pStroke)
        }
    }

    fun getAsBitmap(transparent: Boolean = true, paint: Paint = pStroke): Bitmap =
        Bitmap.createBitmap(width - paddingLeft - paddingRight, height - paddingTop - paddingBottom, Bitmap.Config.ARGB_8888).also { bmp ->
            bmp.applyCanvas {
                withTranslation(-paddingLeft.toFloat(), -paddingTop.toFloat()) {
                    if (!transparent) {
                        drawColor(Color.WHITE)
                    }
                    drawPaths(this, paint)
                }
            }
        }

    public override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.signature = signature
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            signature = state.signature
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {

        var signature: Signature? = null

        constructor(source: Parcel) : super(source) {
            signature = Signature().apply {
                readFromParcel(source)
            }
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            signature?.writeToParcel(out)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    class Signature {
        private val paths = ArrayList<SignaturePath>()

        val isEmpty get() = paths.isEmpty()
        fun clear() = paths.clear()

        fun drawTo(canvas: Canvas, pStroke: Paint) {
            paths.map(SignaturePath::asPath).forEach { canvas.drawPath(it, pStroke) }
        }

        fun addAll(signature: Signature) {
            paths.addAll(signature.paths)
        }

        fun add(currentPath: SignaturePath) {
            paths.add(currentPath)
        }

        fun readFromParcel(pIn: Parcel) {
            val count = pIn.readInt()
            paths.clear()
            repeat(count) {
                add(
                    SignaturePath().apply {
                        readFromParcel(pIn)
                    }
                )
            }
        }

        fun writeToParcel(pOut: Parcel) {
            pOut.writeInt(paths.size)
            paths.forEach { p ->
                p.writeToParcel(pOut)
            }
        }
    }

    class SignaturePath {

        private val points = ArrayList<PointF>()

        fun moveTo(x: Float, y: Float) {
            points.add(PointF(x, y))
        }

        fun lineTo(x: Float, y: Float) {
            points.add(PointF(x, y))
        }

        fun asPath() = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    moveTo(point.x, point.y)
                } else {
                    lineTo(point.x, point.y)
                }
            }
        }

        fun readFromParcel(pIn: Parcel) {
            val count = pIn.readInt()
            points.clear()
            repeat(count) {
                val x = pIn.readFloat()
                val y = pIn.readFloat()
                points.add(PointF(x, y))
            }
        }

        fun writeToParcel(pOut: Parcel) {
            pOut.writeInt(points.size)
            points.forEach { p ->
                pOut.writeFloat(p.x)
                pOut.writeFloat(p.y)
            }
        }
    }
}
