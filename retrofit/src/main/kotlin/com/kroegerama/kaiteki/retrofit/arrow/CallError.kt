package com.kroegerama.kaiteki.retrofit.arrow

import androidx.compose.runtime.Immutable
import androidx.paging.PagingSource
import okhttp3.ResponseBody
import java.io.IOException

@Immutable
sealed interface TypedCallError<out E> {
    val code: Int?
    val cause: Throwable
}
sealed interface CallError : TypedCallError<Nothing>

data class HttpError(
    override val code: Int,
    val message: String,
    val body: ResponseBody?,
    override val cause: Throwable = RuntimeException("HTTP $code")
) : CallError

data class TypedHttpError<out E : Any>(
    override val code: Int,
    val message: String,
    val body: E,
    override val cause: Throwable = RuntimeException("HTTP $code")
) : TypedCallError<E> {
    fun asHttpError() = HttpError(
        code = code,
        message = message,
        body = null,
        cause = cause
    )
}

data class IOError(
    override val cause: IOException
) : CallError {
    override val code: Int? = null
}

data class UnexpectedError(
    override val cause: Throwable
) : CallError {
    override val code: Int? = null
}

data class CallErrorException(
    val delegate: CallError
) : RuntimeException(
    when (delegate) {
        is HttpError -> "HTTP ${delegate.code}"
        is IOError -> delegate.cause.run { localizedMessage ?: message ?: javaClass.simpleName }
        is UnexpectedError -> delegate.cause.run { localizedMessage ?: message ?: javaClass.simpleName }
    },
    delegate.cause
)

fun TypedCallError<*>.exception() = CallErrorException(
    when (this) {
        is TypedHttpError -> asHttpError()
        is HttpError -> this
        is IOError -> this
        is UnexpectedError -> this
    }
)
fun <Key : Any, Value : Any> TypedCallError<*>.loadResultError() = PagingSource.LoadResult.Error<Key, Value>(exception())
