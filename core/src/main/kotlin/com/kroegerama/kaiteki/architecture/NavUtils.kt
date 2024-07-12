package com.kroegerama.kaiteki.architecture

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import java.util.logging.Logger

private val navLabelArgRegex by lazy { """[{](\S+?)[}]""".toRegex() }
private val logger = Logger.getLogger("NavUtils")

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

private fun NavDestination.findClassName(): String? = when (this) {
    is FragmentNavigator.Destination -> className
    is DialogFragmentNavigator.Destination -> className
    is NavGraph -> findStartDestination().findClassName()
    else -> null
}

fun Fragment.navigateSafe(
    directions: NavDirections,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val navController = findNavController()
    val currentClassName = navController.currentDestination?.findClassName()

    var currentFragmentInHierarchy: Fragment? = this
    while (currentFragmentInHierarchy != null) {
        if (currentFragmentInHierarchy.javaClass.name == currentClassName) {
            logger.info("navigate because $currentClassName was in hierarchy")
            navController.navigate(directions.actionId, directions.arguments, navOptions, navigatorExtras)
            return
        }
        currentFragmentInHierarchy = currentFragmentInHierarchy.parentFragment
    }
    logger.warning("ignored navigation to $directions; own destination was ${javaClass.name}, but currentDestination was $currentClassName")
}
