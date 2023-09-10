package com.kroegerama.kaiteki.flow

import kotlinx.coroutines.flow.Flow

class UpdatableFlow<T>(
    flow: Flow<T>,
    private val updateFun: () -> Unit
) : Flow<T> by flow {
    fun update() = updateFun()
}

fun <T> Flow<T>.updatable(
    updateFun: () -> Unit
) = UpdatableFlow(this, updateFun)
