package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@PublishedApi
internal class FlowObserver<T>(
    private val flow: Flow<T>,
    private val collector: (suspend (T) -> Unit)? = null
) : DefaultLifecycleObserver {

    private var job: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch {
            if (collector == null) {
                flow.collect()
            } else {
                flow.collect(collector)
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }
}

inline fun <reified T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    noinline block: suspend (T) -> Unit
) = lifecycleOwner.lifecycle.addObserver(FlowObserver(this, block))

inline fun <reified T> Flow<T>.observeIn(
    lifecycleOwner: LifecycleOwner
) = lifecycleOwner.lifecycle.addObserver(FlowObserver(this))

fun <T> Flow<T>.collectLatestWhenStarted(
    lifecycleOwner: LifecycleOwner,
    block: suspend (T) -> Unit
) = lifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
    collectLatest(block)
}
