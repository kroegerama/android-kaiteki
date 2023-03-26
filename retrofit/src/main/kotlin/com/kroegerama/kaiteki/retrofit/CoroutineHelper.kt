package com.kroegerama.kaiteki.retrofit

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import kotlin.random.Random

internal typealias ApiFun<T> = suspend () -> Response<T>
internal typealias RenewFun<T> = suspend (counter: Int, response: Response<T>) -> Boolean

val DefaultRenewFun: RenewFun<*> = { _, r -> r.code() == 401 }

suspend fun <T> retrofitCall(
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
        } else {
            lastResult = RetrofitResource.NoSuccess(response.code(), response.errorBody(), response.raw())

            val doRenew = counter < retryCount && renewFun(counter, response)
            if (doRenew) {
                delay(Random.nextLong(50, 500))
            } else {
                return lastResult
            }
        }
    }
    return lastResult
}

fun <T> CoroutineScope.retrofitListing(
    resultLiveData: MutableLiveData<RetrofitResource<T>> = MutableLiveData(),
    stateLiveData: MutableLiveData<ListingState> = MutableLiveData(),
    launchNow: Boolean = true,
    renewFun: RenewFun<T> = DefaultRenewFun,
    retryCount: Int = 0,
    block: ApiFun<T>
): Listing<T> {
    stateLiveData.value = ListingState.IDLE

    var job: Job? = null

    fun update() {
        job?.cancel()
        job = launch {
            withContext(Dispatchers.Main) { stateLiveData.value = ListingState.RUNNING }

            val wrappedRenew: RenewFun<T> = { count, response ->
                renewFun(count, response).also { doRenew ->
                    if (doRenew) {
                        withContext(Dispatchers.Main) { stateLiveData.value = ListingState.RETRYING }
                    }
                }
            }
            val response = retrofitCall(wrappedRenew, retryCount, block)
            withContext(Dispatchers.Main) {
                resultLiveData.value = response
                stateLiveData.value = ListingState.FINISHED
            }
            job = null
        }
    }
    if (launchNow) {
        update()
    }

    val cancel: () -> Unit = {
        job?.run {
            if (isActive) {
                cancel()
                stateLiveData.value = ListingState.IDLE
            }
        }
        job = null
    }
    return Listing(resultLiveData, stateLiveData, ::update, cancel)
}
