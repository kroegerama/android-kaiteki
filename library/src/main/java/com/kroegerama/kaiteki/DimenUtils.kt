package com.kroegerama.kaiteki

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.annotation.Px
import com.kroegerama.kaiteki.baseui.BaseFragment

fun Float.dpToPxF() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
@Px
fun Float.dpToPx() = dpToPxF().toInt()

fun Float.spToPxF() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
@Px
fun Float.spToPx() = spToPxF().toInt()

fun Float.spToDp() = this.spToPx() / Resources.getSystem().displayMetrics.scaledDensity

fun Int.dpToPxF() = toFloat().dpToPxF()
@Px
fun Int.dpToPx() = toFloat().dpToPx()

fun Int.spToPxF() = toFloat().spToPxF()
@Px
fun Int.spToPx() = toFloat().spToPx()

/** @see Resources.getDimension */
fun Context.getDimension(@DimenRes res: Int) = resources.getDimension(res)

/** @see Resources.getDimensionPixelSize */
fun Context.getDimensionPixelSize(@DimenRes res: Int) = resources.getDimensionPixelSize(res)

/** @see Resources.getDimensionPixelOffset */
fun Context.getDimensionPixelOffset(@DimenRes res: Int) = resources.getDimensionPixelOffset(res)

/** @see Resources.getDimension */
fun BaseFragment.getDimension(@DimenRes res: Int) = requireContext().getDimension(res)

/** @see Resources.getDimensionPixelSize */
fun BaseFragment.getDimensionPixelSize(@DimenRes res: Int) = requireContext().getDimensionPixelSize(res)

/** @see Resources.getDimensionPixelOffset */
fun BaseFragment.getDimensionPixelOffset(@DimenRes res: Int) = requireContext().getDimensionPixelOffset(res)

