package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Deprecated("use collectLatestWhenLifecycleState instead", ReplaceWith("launchAndCollectLatestWithLifecycleState(lifecycleOwner) { block() }"))
fun <T> Flow<T>.collectLatestWhenStarted(
    lifecycleOwner: LifecycleOwner,
    block: suspend (T) -> Unit
) = lifecycleOwner.lifecycleScope.launch {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { collectLatest(block) }
}

fun <T> Flow<T>.launchAndCollectLatestWithLifecycleState(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend (T) -> Unit
) = lifecycleOwner.lifecycleScope.launch {
    lifecycleOwner.repeatOnLifecycle(state) { collectLatest(block) }
}

fun <T> Flow<T>.launchAndCollectWithLifecycleState(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend (T) -> Unit
) = lifecycleOwner.lifecycleScope.launch {
    lifecycleOwner.repeatOnLifecycle(state) { collect(block) }
}
