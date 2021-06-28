package com.kroegerama.kaiteki.architecture

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController

private val navLabelArgRegex by lazy { """[{](\S+?)[}]""".toRegex() }

fun NavDestination.labelWithArgs(args: Bundle?): CharSequence? = with(label) {
    if (isNullOrBlank() || args == null || !contains('{') || !contains('}')) return this
    return replace(navLabelArgRegex) { r ->
        val key = r.groupValues[1]
        if (args.containsKey(key)) {
            args.get(key)?.toString() ?: r.value
        } else {
            r.value
        }
    }
}

fun Fragment.navigate(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = findNavController().navigate(resId, args, navOptions, navigatorExtras)

fun Fragment.popBackStack() = findNavController().popBackStack()

fun Fragment.popBackStack(
    @IdRes destinationId: Int,
    inclusive: Boolean = false
) = findNavController().popBackStack(destinationId, inclusive)

fun Fragment.navigate(
    directions: NavDirections,
    navOptions: NavOptions? = null
) = findNavController().navigate(directions, navOptions)

fun Fragment.navigate(
    directions: NavDirections,
    navigatorExtras: Navigator.Extras
) = findNavController().navigate(directions, navigatorExtras)
