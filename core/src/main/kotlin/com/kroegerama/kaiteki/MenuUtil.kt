package com.kroegerama.kaiteki

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

operator fun Menu.get(@IdRes idRes: Int): MenuItem? = findItem(idRes)

fun Fragment.findMenuHost(): MenuHost? {
    return when (this) {
        is MenuHostOwner -> menuHost
        is MenuHost -> this
        else -> parentFragment?.findMenuHost() ?: activity?.findMenuHost()
    }
}

fun ComponentActivity.findMenuHost(): MenuHost {
    return when (this) {
        is MenuHostOwner -> menuHost
        else -> this
    }
}

fun MenuHost.addMenuProvider(
    owner: LifecycleOwner,
    @MenuRes menuRes: Int,
    prepareMenu: (Menu) -> Unit = {},
    menuClosed: (Menu) -> Unit = {},
    menuItemSelected: (MenuItem) -> Boolean
) = menuProvider(menuRes, prepareMenu, menuClosed, menuItemSelected).also { provider ->
    addMenuProvider(provider, owner, Lifecycle.State.STARTED)
}

fun menuProvider(
    @MenuRes menuRes: Int,
    prepareMenu: (Menu) -> Unit = {},
    menuClosed: (Menu) -> Unit = {},
    menuItemSelected: (MenuItem) -> Boolean
): MenuProvider = object : MenuProvider {

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(menuRes, menu)
    }

    override fun onPrepareMenu(menu: Menu) = prepareMenu(menu)
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = menuItemSelected(menuItem)
    override fun onMenuClosed(menu: Menu) = menuClosed(menu)
}

interface MenuHostOwner {
    val menuHost: MenuHost
}

interface MenuProviderOwner {
    val menuProvider: MenuProvider
}
