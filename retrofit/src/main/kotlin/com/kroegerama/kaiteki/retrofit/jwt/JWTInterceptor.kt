package com.kroegerama.kaiteki.retrofit.jwt

import android.os.ConditionVariable
import com.kroegerama.kaiteki.retrofit.retrofitCall
import com.kroegerama.kaiteki.tryOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class JWTInterceptor<T>(
    private val callbacks: Callbacks<T>
) : Interceptor {

    interface Callbacks<T> {
        fun getJWT(tokenSet: T): String?
        fun needsToken(request: Request): Boolean
        suspend fun getCurrentToken(): T?
        suspend fun getNewToken(tokenSet: T): retrofit2.Response<T>
        suspend fun onNewToken(newToken: T)
        suspend fun onRefreshFailure(refreshError: RefreshError)
    }

    private val lock = ConditionVariable(true)
    private val isRefreshing = AtomicBoolean(false)

    private fun isExpired(): Boolean {
        val token = runBlocking { callbacks.getCurrentToken()?.let(callbacks::getJWT) } ?: return false.also { Timber.d("Token is null") }
        val jwt = tryOrNull { JWT(token) } ?: return false.also { Timber.d("Token cannot be parsed") }
        jwt.expiresAt?.let { Timber.d("Token expires $it") }
        return jwt.isExpired.also { Timber.d("Token expired: $it") }
    }

    private fun refreshToken(): RefreshError? = runBlocking {
        val currentToken: T = callbacks.getCurrentToken() ?: return@runBlocking RefreshError.Failure(IllegalStateException("Current Token is null."))
        val refreshResponse = retrofitCall(retryCount = 3) { callbacks.getNewToken(currentToken) }
        val newToken = refreshResponse.noSuccess {
            return@runBlocking RefreshError.NoSuccess(code)
        }.error {
            return@runBlocking RefreshError.Failure(throwable)
        }.getOrNull() ?: return@runBlocking RefreshError.Failure(IllegalStateException("Got null response."))
        callbacks.onNewToken(newToken)
        null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!callbacks.needsToken(request)) return chain.proceed(request)
        if (!isExpired()) return chain.proceed(request)

        if (isRefreshing.compareAndSet(false, true)) {
            Timber.d("close lock...")
            lock.close()
            val refreshError = refreshToken()
            if (refreshError != null) {
                runBlocking {
                    callbacks.onRefreshFailure(refreshError)
                }
            }
            Timber.d("open lock...")
            lock.open()
            isRefreshing.set(false)
        } else {
            Timber.d("wait for open...")
            val opened = lock.block(2000)
            Timber.d("opened $opened")
            if (!opened) {
                // timeout while refreshing token in another thread...
                Timber.d("timeout... force token refresh...")
                val refreshError = refreshToken()
                if (refreshError != null) {
                    runBlocking {
                        callbacks.onRefreshFailure(refreshError)
                    }
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
