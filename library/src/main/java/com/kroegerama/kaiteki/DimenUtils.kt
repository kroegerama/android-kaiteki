package com.kroegerama.kaiteki

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.annotation.Px
import com.kroegerama.kaiteki.baseui.BaseFragment

@Px
fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics).toInt()

@Px
fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics).toInt()

fun Float.spToDp() = this.spToPx() / Resources.getSystem().displayMetrics.scaledDensity

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

