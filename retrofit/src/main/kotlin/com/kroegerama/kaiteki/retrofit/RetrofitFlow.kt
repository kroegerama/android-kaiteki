package com.kroegerama.kaiteki.retrofit

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


suspend fun <T> retrofitFlowCall(
    renewFun: RenewFun<T> = DefaultRenewFun,
    retryCount: Int = 0,
    block: ApiFun<T>
): RetrofitResource<T> {
    lateinit var lastResult: RetrofitResource<T>
    repeat(retryCount + 1) { counter ->
        val response = try {
            withContext(Dispatchers.IO) { block.invoke() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            return RetrofitResource.Error(e)
        }
        if (response.isSuccessful) {
            return RetrofitResource.Success(response.body(), response.raw())
        }
        lastResult = RetrofitResource.NoSuccess(response.code(), response.errorBody(), response.raw())
        val doRenew = counter < retryCount && renewFun(counter, response)
        if (doRenew) {
            delay(Random.nextLong(50, 500))
        } else {
            return lastResult
        }
    }
    return lastResult
}


fun <T> CoroutineScope.retrofitFlow(
    launchNow: Boolean = true,
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    renewFun: RenewFun<T> = DefaultRenewFun,
    retryCount: Int = 0,
    block: ApiFun<T>
): UpdatableFlow<RetrofitResource<T>> {
    val flow = MutableSharedFlow<RetrofitResource<T>>(replay, extraBufferCapacity, onBufferOverflow)
    fun updateFun() {
        launch {
            with(flow) {
                emit(RetrofitResource.Running(0))
                val wrappedRenew: RenewFun<T> = { count, response ->
                    renewFun(count, response).also { doRenew ->
                        if (doRenew) {
                            emit(RetrofitResource.Running(count))
                        }
                    }
                }
                val response = retrofitFlowCall(wrappedRenew, retryCount, block)
                emit(response)
            }
        }
    }

    var launched = !launchNow
    return UpdatableFlow(flow.asSharedFlow().onSubscription {
        if (!launched) {
            updateFun()
            launched = true
        }
    }, ::updateFun)
}

class UpdatableFlow<T>(
    flow: SharedFlow<T>,
    private val updateFun: () -> Unit
) : SharedFlow<T> by flow {
    fun update() = updateFun.invoke()
}
