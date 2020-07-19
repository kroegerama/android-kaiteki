package com.kroegerama.kaiteki.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import java.io.Closeable

class Listing<T>(
    val result: LiveData<RetrofitResponse<T>>,
    val state: LiveData<ListingState>,
    private val updateFun: () -> Unit,
    private val cancelFun: () -> Unit
) : Closeable {
    override fun close() = cancel()

    fun update() = updateFun.invoke()
    fun cancel() = cancelFun.invoke()
}

fun <T> LiveData<Listing<T>>.result() = switchMap { it.result }
fun <T> LiveData<Listing<T>>.isRunning() = switchMap { it.state }.map { it.isRunning }
fun LiveData<Listing<*>>.update() = value?.update()
fun LiveData<Listing<*>>.cancel() = value?.cancel()