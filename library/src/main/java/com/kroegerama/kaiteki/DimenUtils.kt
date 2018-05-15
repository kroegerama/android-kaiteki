package com.kroegerama.kaiteki

import android.content.res.Resources
import android.util.TypedValue

fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics).toInt()
fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics).toInt()
fun Float.spToDp() = this.spToPx() / Resources.getSystem().displayMetrics.scaledDensity;
