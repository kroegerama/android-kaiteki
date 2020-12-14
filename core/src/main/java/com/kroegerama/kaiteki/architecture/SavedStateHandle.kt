package com.kroegerama.kaiteki.architecture

import androidx.lifecycle.SavedStateHandle
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.field(key: String? = null) = object : ReadWriteProperty<Any, T?> {
    private fun getKey(property: KProperty<*>) = key ?: property.name
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) = set(getKey(property), value)
    override fun getValue(thisRef: Any, property: KProperty<*>): T? = get(getKey(property))
}

inline fun <reified T : Enum<*>> SavedStateHandle.enumField(key: String? = null) = object : ReadWriteProperty<Any, T?> {
    private fun getKey(property: KProperty<*>) = key ?: property.name
    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) = set<Int>(getKey(property), value?.ordinal)
    override fun getValue(thisRef: Any, property: KProperty<*>): T? = get<Int>(getKey(property))?.let(enumValues<T>()::get)
}
