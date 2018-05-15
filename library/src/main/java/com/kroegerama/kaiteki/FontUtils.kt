package com.kroegerama.kaiteki

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.util.TypedValue

enum class TypefaceStyle(val value: Int) {
    NORMAL(Typeface.NORMAL),
    ITALIC(Typeface.ITALIC),
    BOLD(Typeface.BOLD)
}

fun Context.getThemeTypeface(style: TypefaceStyle): Typeface {
    val outValue = TypedValue()
    theme.resolveAttribute(android.R.attr.fontFamily, outValue, true)
    val res = outValue.resourceId
    if (res == 0) {
        return Typeface.DEFAULT
    }
    try {
        val family = ResourcesCompat.getFont(this, res)
        return Typeface.create(family, style.value)
    } catch (e: Exception) {
        Log.w("Font", "Using FontFamily fallback", e)
    }

    return Typeface.defaultFromStyle(style.value)
}