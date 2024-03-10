package com.kroegerama.kaiteki.retrofit.arrow

import androidx.paging.PagingSource
import okhttp3.ResponseBody
import java.io.IOException

sealed interface TypedCallError<out E> {
    val code: Int?
}
sealed interface CallError : TypedCallError<Nothing>

data class HttpError(
    override val code: Int,
    val message: String,
    val body: ResponseBody?
) : CallError

data class TypedHttpError<out E : Any>(
    override val code: Int,
    val message: String,
    val body: E
) : TypedCallError<E>

data class IOError(
    val cause: IOException
) : CallError {
    override val code: Int? = null
}

data class UnexpectedError(
    val cause: Throwable
) : CallError {
    override val code: Int? = null
}

data class ThrowableCallError(
    val delegate: CallError
) : Throwable(
    message = when (delegate) {
        is HttpError -> "HTTP ${delegate.code}"
        is IOError -> delegate.cause.message
        is UnexpectedError -> delegate.cause.message
    },
    cause = when (delegate) {
        is HttpError -> null
        is IOError -> delegate.cause
        is UnexpectedError -> delegate.cause
    }
)

fun CallError.throwable() = ThrowableCallError(this)
fun <Key : Any, Value : Any> CallError.loadResultError() = PagingSource.LoadResult.Error<Key, Value>(throwable())
