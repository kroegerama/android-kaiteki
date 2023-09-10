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
