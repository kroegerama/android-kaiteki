package com.kroegerama.kaiteki.preference

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun getKey(keySet: String?, property: KProperty<*>) = keySet ?: "${property.name}Key"

class PreferenceFieldBinder<T : Any>(
        private val pref: SharedPreferences,
        private val default: T,
        private val key: String?
) : ReadWriteProperty<Any, T> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T = readValue(property)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        saveNewValue(property, value)
    }

    private fun saveNewValue(property: KProperty<*>, value: T?) {
        pref.edit().apply { putValue(value, getKey(key, property)) }.apply()
    }

    private fun readValue(property: KProperty<*>): T {
        return pref.getValue(property)
    }

    private fun SharedPreferences.getValue(property: KProperty<*>): T {
        val key = getKey(key, property)
        return getFromPreference(default, key)
    }
}

class PreferenceFieldBinderNullable<T : Any?>(
        private val pref: SharedPreferences,
        private val key: String?
) : ReadWriteProperty<Any, T?> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T? = readValue(property)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        saveNewValue(property, value)
    }

    private fun saveNewValue(property: KProperty<*>, value: T?) {
        pref.edit().apply { putValue(value, getKey(key, property)) }.apply()
    }

    private fun readValue(property: KProperty<*>): T? {
        return pref.getValue(property)
    }

    private fun SharedPreferences.getValue(property: KProperty<*>): T? {
        val key = getKey(key, property)
        return getFromPreferenceNullable(key)
    }
}

internal fun SharedPreferences.Editor.putValue(value: Any?, key: String) {
    when (value) {
        null -> remove(key)
        is Long -> putLong(key, value)
        is Int -> putInt(key, value)
        is String -> putString(key, value)
        is Boolean -> putBoolean(key, value)
        is Float -> putFloat(key, value)
        else -> throw IllegalArgumentException("type not allowed")
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any?> SharedPreferences.getFromPreferenceNullable(key: String): T? =
        if (!contains(key)) null else all[key] as? T

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> SharedPreferences.getFromPreference(default: T, key: String): T =
        when (default) {
            is Long -> getLong(key, default as? Long ?: 0L) as T
            is Int -> getInt(key, default as? Int ?: 0) as T
            is String -> getString(key, default as? String ?: "") as T
            is Boolean -> getBoolean(key, default as? Boolean ?: false) as T
            is Float -> getFloat(key, default as? Float ?: 0f) as T
            else -> throw  IllegalArgumentException("type not allowed")
        }