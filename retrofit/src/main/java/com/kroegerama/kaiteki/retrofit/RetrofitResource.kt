package com.kroegerama.kaiteki.retrofit

import com.kroegerama.kaiteki.retrofit.pagination.RetryFun
import okhttp3.Response
import okhttp3.ResponseBody

sealed class RetrofitResource<out TSuccess> {

    object Cancelled : RetrofitResource<Nothing>()

    data class Running(
        val currentRetry: Int
    ) : RetrofitResource<Nothing>()

    data class Success<out T>(
        val data: T?,
        val rawResponse: Response
    ) : RetrofitResource<T>()

    data class NoSuccess(
        val code: Int,
        val errorBody: ResponseBody?,
        val rawResponse: Response
    ) : RetrofitResource<Nothing>()

    data class Error(
        val throwable: Throwable
    ) : RetrofitResource<Nothing>()

    val running get() = this is Running

    val isSuccess get() = this is Success
    val isNoSuccess get() = this is NoSuccess
    val isCancelled get() = this === Cancelled
    val isError get() = this is Error

    fun getOrNull(): TSuccess? = (this as? Success)?.data

    fun <E> map(mapFun: (TSuccess?) -> E?) = if (this is Success) Success(mapFun(data), rawResponse) else this

    inline fun success(block: Success<TSuccess>.() -> Unit): RetrofitResource<TSuccess> {
        if (this is Success) {
            block(this)
        }
        return this
    }

    inline fun noSuccess(block: NoSuccess.() -> Unit): RetrofitResource<TSuccess> {
        if (this is NoSuccess) {
            block(this)
        }
        return this
    }

    inline fun error(block: Error.() -> Unit): RetrofitResource<TSuccess> {
        if (this is Error) {
            block(this)
        }
        return this
    }

    inline fun noSuccessOrError(block: RetrofitResource<TSuccess>.() -> Unit): RetrofitResource<TSuccess> {
        if (this is NoSuccess || this is Error) {
            block(this)
        }
        return this
    }

}

data class RetryableRetrofitResource<out T>(
    val resource: RetrofitResource<T>,
    private val retryFun: RetryFun?
) {
    fun retry() = retryFun?.invoke()
}