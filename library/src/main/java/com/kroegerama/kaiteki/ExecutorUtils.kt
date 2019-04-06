package com.kroegerama.kaiteki

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor

enum class ExecutorState {
    Idle,
    Running,
    Success,
    Error
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
                    ExecutorState.Error
                }
        )
    }
    return liveData
}