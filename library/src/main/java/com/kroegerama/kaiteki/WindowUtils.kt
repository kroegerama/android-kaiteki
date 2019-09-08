package com.kroegerama.kaiteki

import android.os.Build
import android.view.Window

fun Window.noTitle() = requestFeature(Window.FEATURE_NO_TITLE)

fun Window.swipeToDismiss() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
    requestFeature(Window.FEATURE_SWIPE_TO_DISMISS)
} else false