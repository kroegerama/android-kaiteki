package com.kroegerama.kaiteki.retrofit

import kotlinx.coroutines.CancellationException
import retrofit2.Response

@DslMarker
annotation class MultiCallDsl

class MultiCallRaiserCancellationException(
    val response: Response<*>
) : CancellationException()

object ResultBuilderContext {
    @MultiCallDsl
    suspend inline fun <T> bind(
        crossinline block: suspend () -> Response<out T>
    ): T {
        val response = block()
        if (!response.isSuccessful) {
            throw MultiCallRaiserCancellationException(
                response = response
            )
        }
        return response.body()!!
    }
}

@MultiCallDsl
suspend inline fun <reified T> multiCall(
    crossinline block: suspend ResultBuilderContext.() -> T
): Response<T> {
    return try {
        Response.success(
            block(ResultBuilderContext)
        )
    } catch (e: MultiCallRaiserCancellationException) {
        e.response.map { null }
    }
}
