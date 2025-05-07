package com.kroegerama.kaiteki

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update

class SimpleDataSource<T>(
    private val scope: CoroutineScope,
    private val data: Flow<T>,
    val loading: StateFlow<Boolean>,
    private val refreshFun: () -> Unit,
    private val sharingStarted: SharingStarted
) {
    val flow = data.shareIn(scope, sharingStarted, 1)
    fun refresh() = refreshFun()
    val latest get() = flow.replayCache.lastOrNull()

    fun <R> map(transform: suspend (value: T) -> R): SimpleDataSource<R> = SimpleDataSource(
        scope = scope,
        data = data.map(transform),
        loading = loading,
        refreshFun = refreshFun,
        sharingStarted = sharingStarted
    )
}

fun <T> simpleDataSource(
    scope: CoroutineScope,
    sharingStarted: SharingStarted = SharingStarted.Lazily,
    block: suspend () -> T
): SimpleDataSource<T> = simpleDataSource(
    scope,
    flowOf(Unit),
    sharingStarted
) { block() }

fun <T, P> simpleDataSource(
    scope: CoroutineScope,
    parameterFlow: Flow<P>,
    sharingStarted: SharingStarted = SharingStarted.Lazily,
    block: suspend (parameter: P) -> T
): SimpleDataSource<T> {
    val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    val loadingFlow = MutableStateFlow(false)

    val sourceFlow = combine(refreshTrigger, parameterFlow) { _, parameter ->
        parameter
    }

    val dataFlow = channelFlow {
        sourceFlow.collectLatest { parameter ->
            loadingFlow.update { true }
            send(block(parameter))
            loadingFlow.update { false }
        }
    }
    val refresh: () -> Unit = { refreshTrigger.tryEmit(Unit) }

    return SimpleDataSource(
        scope = scope,
        data = dataFlow,
        loading = loadingFlow,
        refreshFun = refresh,
        sharingStarted = sharingStarted
    )
}
