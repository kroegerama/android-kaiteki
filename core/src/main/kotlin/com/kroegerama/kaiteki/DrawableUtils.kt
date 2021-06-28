package com.kroegerama.kaiteki

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.withStyledAttributes

@RequiresApi(Build.VERSION_CODES.M)
class GravityDrawable(
    center: Drawable,
    background: Drawable? = null,
    gravity: Int = Gravity.CENTER
) : LayerDrawable(arrayOf(background, center)), Animatable {

    init {
        setLayerGravity(1, gravity)
    }

    private val animatable = center as? Animatable

    override fun getIntrinsicWidth() = -1
    override fun getIntrinsicHeight() = -1

    override fun start() {
        animatable?.start()
    }

    override fun stop() {
        animatable?.stop()
    }

    override fun isRunning() = animatable?.isRunning ?: false
}

fun Context.getIndeterminateProgressDrawable(small: Boolean = false): Drawable {
    val value = TypedValue()
    val style = if (small) android.R.attr.progressBarStyleSmall else android.R.attr.progressBarStyle
    theme.resolveAttribute(style, value, false)
    val progressBarStyle = value.data
    val attributes = intArrayOf(android.R.attr.indeterminateDrawable)
    lateinit var drawable: Drawable
    withStyledAttributes(progressBarStyle, attributes) {
        drawable = getDrawableOrThrow(0)
    }
    return drawable
}

@RequiresApi(Build.VERSION_CODES.M)
fun Context.getLoadingPlaceholder(small: Boolean = false) = GravityDrawable(getIndeterminateProgressDrawable(small))