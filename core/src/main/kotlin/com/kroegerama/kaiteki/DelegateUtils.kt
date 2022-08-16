package com.kroegerama.kaiteki

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun <T, V : Any> ReadWriteProperty<T, V?>.fallback(fallback: V) = object : ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = this@fallback.getValue(thisRef, property) ?: fallback
    override fun setValue(thisRef: T, property: KProperty<*>, value: V) = this@fallback.setValue(thisRef, property, value)
}

fun <T, V : Any> PropertyDelegateProvider<T, ReadWriteProperty<T, V?>>.fallback(fallback: V) =
    PropertyDelegateProvider<T, ReadWriteProperty<T, V>> { thisRef, property ->
        this@fallback.provideDelegate(thisRef, property).fallback(fallback)
    }

fun <V : Any> KMutableProperty0<V?>.nonNull(fallback: V): ReadWriteProperty<Any?, V> {
    return object : ReadWriteProperty<Any?, V> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): V = get() ?: fallback
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = set(value)
    }
}

fun <In, Out, T> ReadWriteProperty<In, T>.map(
    postWrite: ((Out, In) -> Unit)? = null,
    mapper: (Out) -> In
): ReadWriteProperty<Out, T> = object : ReadWriteProperty<Out, T> {
    override fun getValue(thisRef: Out, property: KProperty<*>): T = this@map.getValue(mapper(thisRef), property)
    override fun setValue(thisRef: Out, property: KProperty<*>, value: T) {
        val mapped = mapper(thisRef)
        this@map.setValue(mapped, property, value)
        postWrite?.invoke(thisRef, mapped)
    }
}
