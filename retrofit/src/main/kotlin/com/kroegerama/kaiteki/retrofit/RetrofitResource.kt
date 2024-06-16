package com.kroegerama.kaiteki.retrofit

import okhttp3.Response
import okhttp3.ResponseBody

sealed class RetrofitResource<out TSuccess> {

    data class Running<out T>(
        val currentRetry: Int,
        val staleData: T? = null
    ) : RetrofitResource<T>()

    data class Success<out T>(
        val data: T?,
        val rawResponse: Response
    ) : RetrofitResource<T>()

    data class NoSuccess<out T>(
        val code: Int,
        val errorBody: ResponseBody?,
        val rawResponse: Response,
        val staleData: T? = null
    ) : RetrofitResource<T>()

    data class Error<out T>(
        val throwable: Throwable,
        val staleData: T? = null
    ) : RetrofitResource<T>()

    val running get() = this is Running

    val isSuccess get() = this is Success
    val isNoSuccess get() = this is NoSuccess
    val isError get() = this is Error

    @Deprecated("Use dataOrNull instead", ReplaceWith("dataOrNull"))
    fun getOrNull(): TSuccess? = (this as? Success)?.data

    val dataOrNull: TSuccess?
        get() = when (this) {
            is Running -> staleData
            is Success -> data
            is NoSuccess -> staleData
            is Error -> staleData
        }

    inline fun <E> map(mapFun: (TSuccess?) -> E?): RetrofitResource<E> = when (this) {
        is Error -> Error(throwable)
        is NoSuccess -> NoSuccess(code, errorBody, rawResponse)
        is Running -> Running(currentRetry)
        is Success -> Success(mapFun(data), rawResponse)
    }

    inline fun onData(
        always: Boolean = true,
        block: RetrofitResource<TSuccess>.(
            data: TSuccess,
            isStale: Boolean
        ) -> Unit
    ): RetrofitResource<TSuccess> {
        when (this) {
            is Running -> staleData?.let { block(it, true) }
            is Success -> data?.let { block(it, false) }
            is NoSuccess -> if (always) staleData?.let { block(it, true) }
            is Error -> if (always) staleData?.let { block(it, true) }
        }
        return this
    }

    inline fun success(block: Success<TSuccess>.() -> Unit): RetrofitResource<TSuccess> {
        if (this is Success) {
            block(this)
        }
        return this
    }

    inline fun noSuccess(block: NoSuccess<TSuccess>.() -> Unit): RetrofitResource<TSuccess> {
        if (this is NoSuccess) {
            block(this)
        }
        return this
    }

    inline fun error(block: Error<TSuccess>.() -> Unit): RetrofitResource<TSuccess> {
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
    private val retryFun: (() -> Unit)?
) {
    fun retry() = retryFun?.invoke()
}
