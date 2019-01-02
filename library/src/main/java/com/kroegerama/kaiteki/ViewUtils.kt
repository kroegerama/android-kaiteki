package com.kroegerama.kaiteki

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.annotation.DrawableRes

fun View.createBitmap(): Bitmap {
    val ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    measure(ms, ms)
    layout(0, 0, measuredWidth, measuredHeight)
    return Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).also {
        draw(Canvas(it))
        this.forceLayout()
    }
}

val Context.selectableItemBackground
    @DrawableRes
    get() =
        obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).run {
            val backgroundResource = getResourceId(0, 0)
            recycle()
            backgroundResource
        }

val Context.selectableItemBackgroundBorderless
    @DrawableRes
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() =
        obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackgroundBorderless)).run {
            val backgroundResource = getResourceId(0, 0)
            recycle()
            backgroundResource
        }