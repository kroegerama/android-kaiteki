package com.kroegerama.kaiteki

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.annotation.Px
import com.kroegerama.kaiteki.baseui.BaseFragment

fun <T : Number> T.dpToPxF() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)
@Px
fun <T : Number> T.dpToPx() = dpToPxF().toInt()

fun <T : Number> T.spToPxF() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, toFloat(), Resources.getSystem().displayMetrics)
@Px
fun <T : Number> T.spToPx() = spToPxF().toInt()

fun <T : Number> T.spToDp() = this.spToPx() / Resources.getSystem().displayMetrics.scaledDensity

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

