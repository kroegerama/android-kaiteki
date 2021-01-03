package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java.io.Closeable

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, { it?.let(observer) })
}

/**
 * Automatically calls current value's .close() before setting a new value
 */
fun <T : Closeable> LiveData<T>.autoClose(): LiveData<T> = MediatorLiveData<T>().also { mediator ->
    mediator.addSource(this) { newValue ->
        mediator.value?.close()
        mediator.value = newValue
    }
}