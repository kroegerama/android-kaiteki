package com.kroegerama.kaiteki.retrofit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.catch
import arrow.core.raise.either
import com.kroegerama.kaiteki.retrofit.arrow.HttpError
import com.kroegerama.kaiteki.retrofit.arrow.IOError
import com.kroegerama.kaiteki.retrofit.arrow.TypedCallError
import com.kroegerama.kaiteki.retrofit.arrow.TypedHttpError
import com.kroegerama.kaiteki.retrofit.arrow.catchingCall
import com.kroegerama.kaiteki.retrofit.arrow.handleResponse
import com.kroegerama.kaiteki.retrofit.datasource.SimpleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

abstract class NetworkControllerBase<ErrorType, Api> {

    abstract val retrofit: Retrofit
    abstract val errorType: Type
    abstract val api: Api

    suspend inline fun <reified T> apiCall(
        crossinline block: suspend Api.() -> Response<out T>
    ): Either<TypedCallError<ErrorType>, T> = either {
        val response = catchingCall { block(api) }
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
                retrofit.responseBodyConverter<ErrorType>(errorType, arrayOfNulls(0)).convert(errorBody)!!
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

    inline fun <reified T> apiFlow(
        crossinline loading: (Boolean) -> Unit = { },
        crossinline block: suspend Api.() -> Response<out T>
    ): Flow<Either<TypedCallError<ErrorType>, T>> = flow {
        emit(apiCall(block))
    }.onStart {
        loading(true)
    }.onCompletion {
        loading(false)
    }

    inline fun <reified T> simpleDataSource(
        scope: CoroutineScope,
        eager: Boolean = false,
        crossinline block: suspend Api.() -> Response<out T>
    ): SimpleDataSource<Either<TypedCallError<ErrorType>, T>> {
        val refreshKey = MutableStateFlow(0)
        val loadingFlow = MutableStateFlow(false)
        val dataFlow = channelFlow {
            refreshKey.collectLatest {
                loadingFlow.value = true
                send(apiCall { block(api) })
                loadingFlow.value = false
            }
        }.shareIn(scope, if (eager) SharingStarted.Eagerly else SharingStarted.Lazily, 1)

        val refresh: () -> Unit = { refreshKey.value++ }

        return SimpleDataSource(
            flow = dataFlow,
            loading = loadingFlow,
            refreshFun = refresh
        )
    }

    inline fun <reified T> ViewModel.simpleDataSource(
        eager: Boolean = false,
        crossinline block: suspend Api.() -> Response<out T>
    ) = simpleDataSource(viewModelScope, eager, block)

    suspend inline fun <From, reified To> Either<TypedCallError<ErrorType>, From>.then(
        crossinline block: suspend Api.(From) -> Response<To>
    ) = flatMap { apiCall { block(it) } }

}
