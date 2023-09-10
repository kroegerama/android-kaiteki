package com.kroegerama.kaiteki.flow

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.transform

open class SingleEventFlow<T : Any> : Flow<T> {

    private val sharedFlow = MutableSharedFlow<T?>(1, 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val collectorFlow = sharedFlow.filterNotNull().transform {
        sharedFlow.emit(null)
        emit(it)
    }

    override suspend fun collect(collector: FlowCollector<T>) = collectorFlow.collect(collector)

    open fun emit(value: T) {
        sharedFlow.tryEmit(value)
    }

    fun asFlow() = collectorFlow
}

class SingleEventUnitFlow : SingleEventFlow<Unit>() {
    fun emit() = emit(Unit)
}
