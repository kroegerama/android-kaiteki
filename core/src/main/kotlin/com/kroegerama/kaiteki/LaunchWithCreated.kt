package com.kroegerama.kaiteki

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStateAtLeast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun LifecycleOwner.launchWithCreated(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.withStateAtLeast(Lifecycle.State.CREATED) {
            launch(context, start, block)
        }
    }
}

fun Fragment.launchWithViewCreated(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        // when fragment lifecycle is created...
        lifecycle.withStateAtLeast(Lifecycle.State.STARTED) {
            // ...use the view lifecycle
            viewLifecycleOwner.launchWithCreated(context, start, block)
        }
    }
}
