package com.kroegerama.kaiteki.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
inline fun <reified Route : Any, reified VM : ViewModel> NavBackStackEntry.hiltSharedViewModel(
    navController: NavController
): VM {
    val backStackEntry = remember(this) {
        navController.getBackStackEntry<Route>()
    }
    return hiltViewModel<VM>(backStackEntry)
}
