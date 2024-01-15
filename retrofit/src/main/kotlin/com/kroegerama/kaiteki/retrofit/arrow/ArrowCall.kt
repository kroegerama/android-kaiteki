package com.kroegerama.kaiteki.retrofit.arrow

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException

@PublishedApi
internal suspend inline fun <reified T> Raise<CallError>.catchingCall(
    crossinline block: suspend () -> Response<out T>
): Response<out T> = catch({
    block()
}) { throwable ->
    raise(
        if (throwable is IOException) {
            IOError(throwable)
        } else {
            UnexpectedError(throwable)
        }
    )
}

@PublishedApi
internal inline fun <reified T> Raise<CallError>.handleResponse(
    response: Response<out T>
): T {
    response.body()?.let { body ->
        return body
    }

    if (response.code() == 204) {
        return catch({
            Unit as T
        }) {
            raise(
                UnexpectedError(
                    IllegalStateException("Response code is ${response.code()} and body is null but <T> is ${T::class}. <T> needs to be Unit.")
                )
            )
        }
    }

    raise(
        UnexpectedError(
            IllegalStateException("Response code is ${response.code()} but body is null.")
        )
    )
}

suspend inline fun <reified T> arrowCall(
    crossinline block: suspend () -> Response<out T>
): Either<CallError, T> = either {
    val response = catchingCall(block)
    if (!response.isSuccessful) {
        val errorBody = response.errorBody()
        raise(
            HttpError(
                code = response.code(),
                message = response.message(),
                body = errorBody
            )
        )
    }
    handleResponse<T>(response)
}

suspend inline fun <reified E, reified T> Retrofit.arrowCall(
    crossinline block: suspend () -> Response<out T>
): Either<TypedCallError<E>, T> = either {
    val response = catchingCall(block)

    if (!response.isSuccessful) {
        val errorBody = response.errorBody()
        if (errorBody == null || errorBody.contentLength() == 0L) {
            raise(
                HttpError(
                    code = response.code(),
                    message = response.message(),
                    body = errorBody
                )
            )
        }
        val errorResponse = catch({
            responseBodyConverter<E>(E::class.java, arrayOfNulls(0)).convert(errorBody)!!
        }) { throwable ->
            raise(
                if (throwable is IOException) {
                    IOError(throwable)
                } else {
                    HttpError(
                        code = response.code(),
                        message = response.message(),
                        body = errorBody
                    )
                }
            )
        }
        raise(
            TypedHttpError(
                code = response.code(),
                message = response.message(),
                body = errorResponse
            )
        )
    }
    handleResponse<T>(response)
}

suspend inline fun <From, reified To> Either<CallError, From>.then(
    crossinline block: suspend (From) -> Response<To>
): Either<CallError, To> = flatMap { arrowCall { block(it) } }

suspend inline fun <reified E, From, reified To> Either<TypedCallError<E>, From>.then(
    retrofit: Retrofit,
    crossinline block: suspend (From) -> Response<To>
): Either<TypedCallError<E>, To> = flatMap { retrofit.arrowCall { block(it) } }