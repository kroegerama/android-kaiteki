package com.kroegerama.kaiteki

import android.content.res.Resources
import android.support.annotation.Px
import android.util.TypedValue

@Px
fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics).toInt()

@Px
fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics).toInt()

fun Float.spToDp() = this.spToPx() / Resources.getSystem().displayMetrics.scaledDensity;
