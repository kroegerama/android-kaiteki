package com.kroegerama.kaiteki.retrofit.jwt

import com.kroegerama.kaiteki.tryOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import java.util.concurrent.atomic.AtomicBoolean

class JWTInterceptor<T>(
    private val callbacks: Callbacks<T>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        interceptSuspending(chain)
    }

    interface Callbacks<T> {
        fun getJWT(tokenSet: T): String?
        fun needsToken(request: Request): Boolean
        suspend fun getCurrentToken(): T?
        suspend fun onNewToken(newToken: T)
        suspend fun onRefreshFailure(refreshError: RefreshError)
        fun createNewTokenCall(tokenSet: T): Call<T>
    }

    private val mutex = Mutex(locked = false)
    private val isRefreshing = AtomicBoolean(false)

    private suspend fun getJWT(tokenSet: T): String? = withContext(Dispatchers.Main) { callbacks.getJWT(tokenSet) }
    private suspend fun needsToken(request: Request): Boolean = withContext(Dispatchers.Main) { callbacks.needsToken(request) }
    private suspend fun getCurrentToken(): T? = withContext(Dispatchers.Main) { callbacks.getCurrentToken() }
    private suspend fun onNewToken(newToken: T) = withContext(Dispatchers.Main) { callbacks.onNewToken(newToken) }
    private suspend fun onRefreshFailure(refreshError: RefreshError) = withContext(Dispatchers.Main) { callbacks.onRefreshFailure(refreshError) }
    private suspend fun getNewToken(tokenSet: T): retrofit2.Response<T> =
        withContext(Dispatchers.IO) { callbacks.createNewTokenCall(tokenSet).execute() }

    private suspend fun isExpired(): Boolean {
        val token = getCurrentToken()?.let { getJWT(it) } ?: return false
        val jwt = tryOrNull { JWT(token) } ?: return false
        return jwt.isExpired
    }

    private suspend fun refreshToken(): RefreshError? {
        val currentToken: T = getCurrentToken() ?: return RefreshError.Failure(IllegalStateException("Current Token is null."))
        val refreshResponse = runCatching {
            getNewToken(currentToken)
        }.onFailure {
            if (it is CancellationException) throw it
        }.getOrElse {
            return RefreshError.Failure(it)
        }
        if (!refreshResponse.isSuccessful) {
            return RefreshError.NoSuccess(refreshResponse.code())
        }
        val newToken = refreshResponse.body() ?: return RefreshError.Failure(IllegalStateException("Got null response."))
        onNewToken(newToken)
        return null
    }

    private suspend fun interceptSuspending(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!needsToken(request)) return chain.proceed(request)
        if (!isExpired()) return chain.proceed(request)

        if (isRefreshing.compareAndSet(false, true)) {
            mutex.withLock {
                val refreshError = refreshToken()
                if (refreshError != null) {
                    onRefreshFailure(refreshError)
                }
            }
            isRefreshing.set(false)
        } else {
            val timedOut = withTimeoutOrNull(2000) {
                mutex.withLock { }
            } == null
            if (timedOut) {
                // timeout while refreshing token in another thread...
                val refreshError = refreshToken()
                if (refreshError != null) {
                    onRefreshFailure(refreshError)
                }
            }
        }
        return chain.proceed(request)
    }

    sealed class RefreshError {
        data class NoSuccess(val code: Int) : RefreshError()
        data class Failure(val t: Throwable) : RefreshError()
    }

}
