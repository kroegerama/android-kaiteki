package com.kroegerama.kaiteki.retrofit

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.ResponseBody
import kotlin.random.Random


suspend fun <T> retrofitFlowCall(
    renewFun: RenewFun<T> = DefaultRenewFun,
    retryCount: Int = 0,
    block: ApiFun<T>
): RetrofitFlow<T> {
    lateinit var lastResult: RetrofitFlow<T>
    repeat(retryCount + 1) { counter ->
        val response = try {
            withContext(Dispatchers.IO) { block.invoke() }
        } catch (c: CancellationException) {
            return RetrofitFlow.Cancelled
        } catch (e: Exception) {
            return RetrofitFlow.Error(e)
        }
        if (response.isSuccessful) {
            return RetrofitFlow.Success(response.body(), response.raw())
        }
        lastResult = RetrofitFlow.NoSuccess(response.code(), response.errorBody(), response.raw())
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
): UpdatableFlow<RetrofitFlow<T>> {
    val flow = MutableSharedFlow<RetrofitFlow<T>>(replay, extraBufferCapacity, onBufferOverflow)
    fun updateFun() {
        launch {
            with(flow) {
                emit(RetrofitFlow.Running(0))
                val wrappedRenew: RenewFun<T> = { count, response ->
                    renewFun(count, response).also { doRenew ->
                        if (doRenew) {
                            emit(RetrofitFlow.Running(count))
                        }
                    }
                }
                val response = retrofitFlowCall(wrappedRenew, retryCount, block)
                emit(response)
            }
        }
    }
    if (launchNow) {
        updateFun()
    }
    return UpdatableFlow(flow.asSharedFlow(), ::updateFun)
}

class UpdatableFlow<T>(
    flow: SharedFlow<T>,
    private val updateFun: () -> Unit
) : SharedFlow<T> by flow {
    fun update() = updateFun.invoke()
}

sealed class RetrofitFlow<out TSuccess> {

    object Cancelled : RetrofitFlow<Nothing>()

    data class Running(
        val currentRetry: Int
    ) : RetrofitFlow<Nothing>()

    data class Success<out T>(
        val data: T?,
        val rawResponse: Response
    ) : RetrofitFlow<T>()

    data class NoSuccess(
        val code: Int,
        val errorBody: ResponseBody?,
        val rawResponse: Response
    ) : RetrofitFlow<Nothing>()

    data class Error(
        val throwable: Throwable
    ) : RetrofitFlow<Nothing>()

}