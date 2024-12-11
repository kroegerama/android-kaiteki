package com.kroegerama.kaiteki.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

interface RestartableStateFlow<out T> : StateFlow<T> {
    fun restart()
}

interface RestartableSharedFlow<out T> : SharedFlow<T> {
    fun restart()
}

private data class SharingRestartable(
    private val sharingStarted: SharingStarted
) : SharingStarted {

    private val commandFlow = MutableSharedFlow<SharingCommand>(extraBufferCapacity = 2)

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> {
        return merge(commandFlow, sharingStarted.command(subscriptionCount))
    }

    fun restart() {
        commandFlow.tryEmit(SharingCommand.STOP_AND_RESET_REPLAY_CACHE)
        commandFlow.tryEmit(SharingCommand.START)
    }

}

fun <T> Flow<T>.restartableStateIn(
    scope: CoroutineScope,
    started: SharingStarted,
    initialValue: T
): RestartableStateFlow<T> {
    val restartable = SharingRestartable(started)
    val stateFlow = stateIn(
        scope = scope,
        started = restartable,
        initialValue = initialValue
    )
    return object : StateFlow<T> by stateFlow, RestartableStateFlow<T> {
        override fun restart() {
            restartable.restart()
        }
    }
}

fun <T> Flow<T>.restartableShareIn(
    scope: CoroutineScope,
    started: SharingStarted,
    replay: Int = 0
): RestartableSharedFlow<T> {
    val restartable = SharingRestartable(started)
    val stateFlow = shareIn(
        scope = scope,
        started = restartable,
        replay = replay
    )
    return object : SharedFlow<T> by stateFlow, RestartableSharedFlow<T> {
        override fun restart() {
            restartable.restart()
        }
    }
}
