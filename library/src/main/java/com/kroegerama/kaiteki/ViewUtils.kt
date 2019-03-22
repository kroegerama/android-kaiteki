package com.kroegerama.kaiteki

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.annotation.DimenRes
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

val Context.listPreferredItemHeight
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeight)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

val Context.listPreferredItemHeightSmall
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeightSmall)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

val Context.listPreferredItemHeightLarge
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeightLarge)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

fun View.showIf(value: Boolean, goneIfFalse: Boolean = true) {
    visibility = when {
        value -> View.VISIBLE
        goneIfFalse -> View.GONE
        else -> View.INVISIBLE
    }
}