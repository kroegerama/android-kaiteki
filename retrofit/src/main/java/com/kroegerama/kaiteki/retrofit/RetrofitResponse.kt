package com.kroegerama.kaiteki.retrofit

import okhttp3.ResponseBody
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class RetrofitResponse<out R> {
    data class Success<out T>(val data: T?) : RetrofitResponse<T>()
    data class NoSuccess(val code: Int, val response: ResponseBody?) : RetrofitResponse<Nothing>()
    object Cancelled : RetrofitResponse<Nothing>()
    data class Error(val exception: Exception) : RetrofitResponse<Nothing>()

    val isSuccess get() = this is Success
    val isNoSuccess get() = this is NoSuccess
    val isCancelled get() = this === Cancelled
    val isError get() = this is Error

    fun getOrNull() = if (this is Success) data else null
    fun <E> map(mapFun: (R?) -> E?) = if (this is Success) Success(mapFun(data)) else this

    // better toString name for objects, data classes will automatically overwrite this
    override fun toString(): String = this::class.java.simpleName
}

data class RetryableRetrofitResponse<T>(
    val response: RetrofitResponse<T>,
    private val retryFun: RetryFun?
) {
    fun retry() = retryFun?.invoke()
}

inline fun <T> RetrofitResponse<T>.success(block: RetrofitResponse.Success<T>.() -> Unit): RetrofitResponse<T> {
    if (this is RetrofitResponse.Success) {
        block(this)
    }
    return this
}

inline fun <T> RetrofitResponse<T>.noSuccessOrError(block: RetrofitResponse<T>.() -> Unit): RetrofitResponse<T> {
    if (this is RetrofitResponse.NoSuccess || this is RetrofitResponse.Error) {
        block(this)
    }
    return this
}

inline fun <T> RetrofitResponse<T>.noSuccess(block: RetrofitResponse.NoSuccess.() -> Unit): RetrofitResponse<T> {
    if (this is RetrofitResponse.NoSuccess) {
        block(this)
    }
    return this
}

inline fun <T> RetrofitResponse<T>.error(block: RetrofitResponse.Error.() -> Unit): RetrofitResponse<T> {
    if (this is RetrofitResponse.Error) {
        block(this)
    }
    return this
}

@ExperimentalContracts
fun <T> RetrofitResponse<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is RetrofitResponse.Success<T>)
    }
    return this is RetrofitResponse.Success
}

@ExperimentalContracts
fun <T> RetrofitResponse<T>.isNoSuccess(): Boolean {
    contract {
        returns(true) implies (this@isNoSuccess is RetrofitResponse.NoSuccess)
    }
    return this is RetrofitResponse.NoSuccess
}

@ExperimentalContracts
fun <T> RetrofitResponse<T>.isCancelled(): Boolean {
    contract {
        returns(true) implies (this@isCancelled is RetrofitResponse.Cancelled)
    }
    return this is RetrofitResponse.Cancelled
}

@ExperimentalContracts
fun <T> RetrofitResponse<T>.isError(): Boolean {
    contract {
        returns(true) implies (this@isError is RetrofitResponse.Error)
    }
    return this is RetrofitResponse.Error
}