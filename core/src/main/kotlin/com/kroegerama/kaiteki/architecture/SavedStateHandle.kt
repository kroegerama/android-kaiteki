package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.SavedStateHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.field(key: String? = null) =
    SavedStateDelegateProvider<T?>(this, key, null)

fun <T : Any> SavedStateHandle.fieldNonNull(fallback: T, key: String? = null) =
    SavedStateDelegateProvider(this, key, fallback)

class SavedStateDelegateProvider<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String?,
    private val fallback: T
) : PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> {
    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = object : ReadWriteProperty<Any?, T> {
        private val actualKey = key ?: property.name
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = savedStateHandle[actualKey] ?: fallback
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = savedStateHandle.set(actualKey, value)
    }
}

inline operator fun <reified T> SavedStateHandle.getValue(thisRef: Any?, property: KProperty<*>): T? = get(property.name)
inline operator fun <reified T> SavedStateHandle.setValue(thisRef: Any?, property: KProperty<*>, value: T?) = set(property.name, value)
