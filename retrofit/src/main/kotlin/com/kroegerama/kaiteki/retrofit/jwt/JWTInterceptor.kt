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
        suspend fun onRefreshFailure()
    }

    private val lock = ConditionVariable(true)
    private val isRefreshing = AtomicBoolean(false)

    private fun isExpired(): Boolean {
        val token = runBlocking { callbacks.getCurrentToken()?.let(callbacks::getJWT) } ?: return false.also { Timber.d("Token is null") }
        val jwt = tryOrNull { JWT(token) } ?: return false.also { Timber.d("Token cannot be parsed") }
        jwt.expiresAt?.let { Timber.d("Token expires $it") }
        return jwt.isExpired.also { Timber.d("Token expired: $it") }
    }

    private fun refreshToken(): Boolean = runBlocking {
        val currentToken = callbacks.getCurrentToken() ?: return@runBlocking false
        val refreshResponse = retrofitCall(retryCount = 3) { callbacks.getNewToken(currentToken) }
        val newToken = refreshResponse.getOrNull() ?: return@runBlocking false
        callbacks.onNewToken(newToken)
        true
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!callbacks.needsToken(request)) return chain.proceed(request)
        if (!isExpired()) return chain.proceed(request)

        if (isRefreshing.compareAndSet(false, true)) {
            Timber.d("close lock...")
            lock.close()
            if (!refreshToken()) {
                runBlocking {
                    callbacks.onRefreshFailure()
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
                if (!refreshToken()) {
                    runBlocking {
                        callbacks.onRefreshFailure()
                    }
                }
            }
        }
        return chain.proceed(request)
    }
}
