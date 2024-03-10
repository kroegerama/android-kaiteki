package com.kroegerama.kaiteki.retrofit.arrow

import androidx.paging.PagingSource
import okhttp3.ResponseBody
import java.io.IOException

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
) : TypedCallError<E>

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

data class TypedCallErrorException(
    val delegate: TypedCallError<*>
) : RuntimeException(
    when (delegate) {
        is TypedHttpError -> "HTTP ${delegate.code}"
        is HttpError -> "HTTP ${delegate.code}"
        is IOError -> delegate.cause.run { localizedMessage ?: message ?: javaClass.simpleName }
        is UnexpectedError -> delegate.cause.run { localizedMessage ?: message ?: javaClass.simpleName }
    },
    delegate.cause
)

fun TypedCallError<*>.exception() = TypedCallErrorException(this)
fun <Key : Any, Value : Any> TypedCallError<*>.loadResultError() = PagingSource.LoadResult.Error<Key, Value>(exception())
