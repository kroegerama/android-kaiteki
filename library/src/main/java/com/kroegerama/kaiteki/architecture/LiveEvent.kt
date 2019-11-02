package com.kroegerama.kaiteki.architecture

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.*

open class LiveEvent<T> : MutableLiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    private val wrappers = WeakHashMap<Observer<in T>, Wrapper<in T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = Wrapper(observer)
        wrappers[observer] = wrapper
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val wrapper = Wrapper(observer)
        wrappers[observer] = wrapper
        super.observeForever(observer)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        val wrapper: Wrapper<in T>? = if (observer is Wrapper) observer else wrappers[observer]
        if (wrapper != null) {
            wrappers -= wrapper
            super.removeObserver(wrapper)
        }
    }

    @MainThread
    override fun setValue(value: T) {
        wrappers.values.forEach(Wrapper<in T>::invalidate)
        super.setValue(value)
    }

    @MainThread
    operator fun invoke(value: T) = setValue(value)

    private class Wrapper<W>(val delegate: Observer<W>) : Observer<W> {

        private var pending = false

        fun invalidate() {
            pending = true
        }

        override fun onChanged(t: W) {
            if (!pending) return

            pending = false
            delegate.onChanged(t)
        }
    }
}

class NotifyLiveEvent : LiveEvent<Unit>() {

    @MainThread
    operator fun invoke() = setValue(Unit)

    fun post() = postValue(Unit)

    fun observe(owner: LifecycleOwner, observer: () -> Unit) =
        observe(owner, Observer { observer.invoke() })

    fun observeForever(observer: () -> Unit) =
        observeForever(Observer { observer.invoke() })
}