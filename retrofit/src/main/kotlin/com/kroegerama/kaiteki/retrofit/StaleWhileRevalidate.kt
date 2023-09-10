package com.kroegerama.kaiteki.retrofit

import android.content.Context
import android.util.LruCache
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import retrofit2.Call
import retrofit2.awaitResponse
import java.io.File
import java.security.MessageDigest

internal fun File.calculateDefaultCacheSize() = freeSpace.div(20).coerceIn(
    1L * 1024 * 1024,
    50L * 1024 * 1024
).toInt()

class StaleWhileRevalidate(
    val cacheDir: File,
    val lruCacheSizeBytes: Int = cacheDir.calculateDefaultCacheSize(),
    @PublishedApi
    internal val moshiProvider: () -> Moshi
) {
    constructor(
        context: Context,
        cacheDir: File = File(context.cacheDir, "stale-revalidate-cache"),
        lruCacheSizeBytes: Int = cacheDir.calculateDefaultCacheSize(),
        moshiProvider: () -> Moshi
    ) : this(cacheDir, lruCacheSizeBytes, moshiProvider)

    @PublishedApi
    internal val lruCache: LruCache<String, File>

    private val digest by lazy { MessageDigest.getInstance("MD5") }

    init {
        cacheDir.mkdirs()
        lruCache = object : LruCache<String, File>(lruCacheSizeBytes) {
            override fun sizeOf(key: String, value: File): Int = value.length().toInt()

            override fun entryRemoved(evicted: Boolean, key: String, oldValue: File, newValue: File?) {
                if (evicted || newValue == null) {
                    oldValue.delete()
                }
            }
        }

        cacheDir.listFiles()?.sortedBy {
            it.lastModified()
        }?.forEach {
            if (it.isFile) {
                lruCache.put(it.name, it)
            }
        }
    }

    inline fun <reified T> retrofitSharedFlow(
        call: Call<T>,
        scope: CoroutineScope,
        started: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): SharedFlow<RetrofitResource<T>> = retrofitFlow(call).shareIn(scope, started, 1)

    inline fun <reified T> retrofitFlow(
        call: Call<T>
    ): Flow<RetrofitResource<T>> = flow {
        val networkCall = call.run {
            if (isExecuted || isCanceled) {
                clone()
            } else {
                this
            }
        }
        if (networkCall.request().method != "GET") {
            emit(RetrofitResource.Running(0, null))
            runCatching {
                networkCall.awaitResponse()
            }.onFailure {
                if (it is CancellationException) {
                    throw it
                }
                emit(RetrofitResource.Error(it, null))
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    emit(RetrofitResource.Success(response.body(), response.raw()))
                } else {
                    emit(RetrofitResource.NoSuccess(response.code(), response.errorBody(), response.raw(), null))
                }
            }

            return@flow
        }

        val key = networkCall.keyHash()
        val cacheFile = lruCache.get(key) ?: File(cacheDir, key)

        val stale = if (cacheFile.exists()) {
            runCatching {
                val json = cacheFile.readText(Charsets.UTF_8)
                moshiProvider().adapter(T::class.java).fromJson(json)
            }.getOrNull()
        } else null
        emit(RetrofitResource.Running(0, stale))

        runCatching {
            networkCall.awaitResponse()
        }.onFailure {
            if (it is CancellationException) {
                throw it
            }
            emit(RetrofitResource.Error(it, stale))
        }.onSuccess { response ->
            if (response.isSuccessful) {
                val data = response.body()
                runCatching {
                    val json = moshiProvider().adapter(T::class.java).toJson(data)
                    lruCache.remove(key)
                    cacheFile.writeText(json, Charsets.UTF_8)
                    lruCache.put(key, cacheFile)
                }.onFailure {
                    if (it is CancellationException) {
                        throw it
                    }
                }
                emit(RetrofitResource.Success(response.body(), response.raw()))
            } else {
                emit(RetrofitResource.NoSuccess(response.code(), response.errorBody(), response.raw(), stale))
            }
        }
    }

    @PublishedApi
    internal fun Call<*>.keyHash(): String {
        val relevantContent = request().run {
            url.toString() + headers.toString()
        }
        val md5Bytes = digest.digest(relevantContent.toByteArray(Charsets.UTF_8))
        return md5Bytes.joinToString("") {
            "%02x".format(it)
        }
    }

}
