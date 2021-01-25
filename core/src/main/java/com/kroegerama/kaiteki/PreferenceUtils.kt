package com.kroegerama.kaiteki

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


fun SharedPreferences.longField(key: String? = null): ReadWriteProperty<Any, Long?> =
    field(key, { k, v -> putLong(k, v) }, { k -> getLong(k, 0) }).map { this }

fun SharedPreferences.intField(key: String? = null): ReadWriteProperty<Any, Int?> =
    field(key, { k, v -> putInt(k, v) }, { k -> getInt(k, 0) }).map { this }

fun SharedPreferences.stringField(key: String? = null): ReadWriteProperty<Any, String?> =
    field(key, { k, v -> putString(k, v) }, { k -> getString(k, null) }).map { this }

fun SharedPreferences.stringSetField(key: String? = null): ReadWriteProperty<Any, Set<String>?> =
    field(key, { k, v -> putStringSet(k, v) }, { k -> getStringSet(k, null) }).map { this }

fun SharedPreferences.booleanField(key: String? = null): ReadWriteProperty<Any, Boolean?> =
    field(key, { k, v -> putBoolean(k, v) }, { k -> getBoolean(k, false) }).map { this }

fun SharedPreferences.floatField(key: String? = null): ReadWriteProperty<Any, Float?> =
    field(key, { k, v -> putFloat(k, v) }, { k -> getFloat(k, 0f) }).map { this }

inline fun <reified T : Enum<*>> SharedPreferences.enumField(key: String? = null): ReadWriteProperty<Any, T?> =
    field<T>(
        key,
        { k, v -> putInt(k, v.ordinal) },
        { k -> getInt(k, -1).let { ord -> if (ord == -1) null else enumValues<T>()[ord] } }
    ).map { this }

private class MappedDelegate<In, Out, T>(
    private val source: ReadWriteProperty<In, T>,
    private val postWrite: ((Out, In) -> Unit)? = null,
    private val mapper: (Out) -> In
) : ReadWriteProperty<Out, T> {

    override fun getValue(thisRef: Out, property: KProperty<*>): T =
        source.getValue(mapper(thisRef), property)

    override fun setValue(thisRef: Out, property: KProperty<*>, value: T) {
        val mapped = mapper(thisRef)
        source.setValue(mapped, property, value)
        postWrite?.invoke(thisRef, mapped)
    }
}

fun <In, Out, T> ReadWriteProperty<In, T>.map(
    postWrite: ((Out, In) -> Unit)? = null,
    mapper: (Out) -> In
): ReadWriteProperty<Out, T> =
    MappedDelegate(source = this, postWrite = postWrite, mapper = mapper)

@PublishedApi
internal fun getKey(customKey: String?, property: KProperty<*>) = customKey ?: "${property.name}Key"

@PublishedApi
internal inline fun <reified T> field(
    key: String? = null,
    crossinline setter: SharedPreferences.Editor.(String, T) -> Unit,
    crossinline getter: SharedPreferences.(String) -> T?
) = object : ReadWriteProperty<SharedPreferences, T?> {
    override fun setValue(thisRef: SharedPreferences, property: KProperty<*>, value: T?) {
        val prefKey = getKey(key, property)
        thisRef.edit { if (value == null) remove(prefKey) else setter(prefKey, value) }
    }

    override fun getValue(thisRef: SharedPreferences, property: KProperty<*>): T? = with(thisRef) {
        val prefKey = getKey(key, property)
        return if (contains(prefKey)) getter(prefKey) else null
    }
}
