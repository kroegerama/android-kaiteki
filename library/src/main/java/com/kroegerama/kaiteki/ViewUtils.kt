package com.kroegerama.kaiteki

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.annotation.DrawableRes

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

fun View.showIf(value: Boolean, goneIfFalse: Boolean = true) {
    visibility = when {
        value -> View.VISIBLE
        goneIfFalse -> View.GONE
        else -> View.INVISIBLE
    }
}