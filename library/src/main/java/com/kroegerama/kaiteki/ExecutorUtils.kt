package com.kroegerama.kaiteki

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor

sealed class ExecutorState {
    object Idle : ExecutorState()
    object Running : ExecutorState()
    object Success : ExecutorState()
    data class Error(val e: Exception? = null) : ExecutorState()
}

fun Executor.executeWithState(block: () -> ExecutorState): LiveData<ExecutorState> {
    val liveData = MutableLiveData<ExecutorState>()
    liveData.value = ExecutorState.Idle
    execute {
        liveData.postValue(ExecutorState.Running)
        liveData.postValue(
                try {
                    block.invoke()
                } catch (e: Exception) {
                    ExecutorState.Error(e)
                }
        )
    }
    return liveData
}