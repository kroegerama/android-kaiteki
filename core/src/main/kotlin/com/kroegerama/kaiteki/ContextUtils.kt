package com.kroegerama.kaiteki

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import com.google.android.material.color.MaterialColors

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

@ColorInt
fun Context.getThemeColor(@AttrRes attrRes: Int, @ColorInt fallback: Int = Color.MAGENTA): Int =
    MaterialColors.getColor(this, attrRes, fallback)

@AnyRes
fun Context.resolveResourceIdAttribute(@AttrRes attrRes: Int): Int = TypedValue().apply {
    theme.resolveAttribute(attrRes, this, true)
}.resourceId
