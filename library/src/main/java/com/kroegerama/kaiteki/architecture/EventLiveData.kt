package com.kroegerama.kaiteki.architecture

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

open class EventLiveData<T> : MutableLiveData<Event<T>>() {

    fun event(value: T) {
        setValue(Event(value))
    }

    fun postEvent(value: T) {
        postValue(Event(value))
    }

    fun observeEvent(owner: LifecycleOwner, observer: Observer<T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Only one Observer at a time is supported.")
        }
        super.observe(owner, EventObserver {
            observer.onChanged(it)
        })

    }
}

open class EventObserver<T>(private val eventHandler: (T) -> Unit) : Observer<Event<T>> {

    override fun onChanged(t: Event<T>?) {
        t?.pop()?.let(eventHandler)
    }
}

open class Event<out T>(private val payload: T) {

    private val pending = AtomicBoolean(true)

    fun pop(): T? {
        return if (pending.compareAndSet(true, false)) {
            payload
        } else {
            null
        }
    }

    val isPending
        get() = pending.get()

    fun peek(): T {
        return payload
    }
}

private const val TAG = "EventLiveData"
