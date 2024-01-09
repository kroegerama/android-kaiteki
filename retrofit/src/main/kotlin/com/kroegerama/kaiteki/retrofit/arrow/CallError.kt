package com.kroegerama.kaiteki.retrofit.arrow

import okhttp3.ResponseBody
import java.io.IOException

sealed interface TypedCallError<out E>
sealed interface CallError : TypedCallError<Nothing>

data class HttpError(
    val code: Int,
    val message: String,
    val body: ResponseBody?
) : CallError

data class TypedHttpError<out E : Any>(
    val code: Int,
    val message: String,
    val body: E
) : TypedCallError<E>

data class IOError(
    val cause: IOException
) : CallError

data class UnexpectedError(
    val cause: Throwable
) : CallError

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
