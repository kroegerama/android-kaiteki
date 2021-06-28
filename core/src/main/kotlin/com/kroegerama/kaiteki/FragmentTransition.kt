package com.kroegerama.kaiteki

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.setupSharedAxisTransition(@MaterialSharedAxis.Axis axis: Int) {
    exitTransition = MaterialSharedAxis(axis, true)
    reenterTransition = MaterialSharedAxis(axis, false)
    enterTransition = MaterialSharedAxis(axis, true)
    returnTransition = MaterialSharedAxis(axis, false)
}

fun Fragment.setupFadeThroughTransition() {
    enterTransition = MaterialFadeThrough()
    exitTransition = MaterialFadeThrough()
}
