package com.kroegerama.kaiteki.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.UUID

typealias TitleProvider = @Composable () -> Unit
typealias ActionsProvider = @Composable RowScope.() -> Unit

@ExperimentalKaitekiComposeApi
@Immutable
class AppBarHost {

    private val titleStack = mutableStateListOf<Pair<UUID, TitleProvider>>()
    private val actionsStack = mutableStateListOf<Pair<UUID, ActionsProvider>>()

    val title
        @Composable get() = remember {
            derivedStateOf {
                titleStack.lastOrNull()?.second
            }
        }

    val actions
        @Composable get() = remember {
            derivedStateOf {
                actionsStack.lastOrNull()?.second
            }
        }

    internal fun setTitle(uuid: UUID, title: TitleProvider) {
        titleStack.removeIf { it.first == uuid }
        titleStack += uuid to title
    }

    internal fun removeTitle(uuid: UUID) {
        titleStack.removeIf { it.first == uuid }
    }

    internal fun setActions(uuid: UUID, actions: ActionsProvider) {
        actionsStack.removeIf { it.first == uuid }
        actionsStack += uuid to actions
    }

    internal fun removeActions(uuid: UUID) {
        actionsStack.removeIf { it.first == uuid }
    }

}

@ExperimentalKaitekiComposeApi
val LocalAppBarHost: ProvidableCompositionLocal<AppBarHost> = staticCompositionLocalOf { AppBarHost() }

@ExperimentalKaitekiComposeApi
@Composable
fun ProvideAppBarTitle(
    appBarHost: AppBarHost = LocalAppBarHost.current,
    title: @Composable () -> Unit
) {
    val titleState by rememberUpdatedState(title)
    val uuid = remember { UUID.randomUUID() }
    SideEffect {
        appBarHost.setTitle(uuid, titleState)
    }
    DisposableEffect(Unit) {
        onDispose {
            appBarHost.removeTitle(uuid)
        }
    }
}

@ExperimentalKaitekiComposeApi
@Composable
fun ProvideEmptyAppBarActions(
    appBarHost: AppBarHost = LocalAppBarHost.current
) = ProvideAppBarActions(appBarHost = appBarHost) { }

@ExperimentalKaitekiComposeApi
@Composable
fun ProvideAppBarActions(
    appBarHost: AppBarHost = LocalAppBarHost.current,
    actions: @Composable RowScope.() -> Unit
) {
    val actionsState by rememberUpdatedState(actions)
    val uuid = remember { UUID.randomUUID() }
    SideEffect {
        appBarHost.setActions(uuid, actionsState)
    }
    DisposableEffect(Unit) {
        onDispose {
            appBarHost.removeActions(uuid)
        }
    }
}
