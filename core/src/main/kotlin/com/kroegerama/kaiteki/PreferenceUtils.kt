package com.kroegerama.kaiteki

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun SharedPreferences.field(fallback: Long, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getLong(key, fallback) },
    setter = { key, value -> putLong(key, value) }
)

fun SharedPreferences.field(fallback: Int, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getInt(key, fallback) },
    setter = { key, value -> putInt(key, value) }
)

fun SharedPreferences.field(fallback: String, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getString(key, fallback) ?: fallback },
    setter = { key, value -> putString(key, value) }
)

fun SharedPreferences.field(fallback: Set<String>, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getStringSet(key, fallback) },
    setter = { key, value -> putStringSet(key, value) }
)

fun SharedPreferences.field(fallback: Boolean, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getBoolean(key, fallback) },
    setter = { key, value -> putBoolean(key, value) }
)

fun SharedPreferences.field(fallback: Float, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getFloat(key, fallback) },
    setter = { key, value -> putFloat(key, value) }
)

inline fun <reified T : Enum<T>> SharedPreferences.field(fallback: T, keyNullable: String? = null) = PreferencesDelegateProvider(
    prefs = this,
    key = keyNullable,
    fallback = fallback,
    getter = { key -> getInt(key, -1).let { if (it == -1) fallback else enumValues<T>()[it] } },
    setter = { key, value -> putInt(key, value.ordinal) }
)

class PreferencesDelegateProvider<T>(
    private val prefs: SharedPreferences,
    private val key: String?,
    private val fallback: T,
    private val getter: SharedPreferences.(String) -> T,
    private val setter: SharedPreferences.Editor.(String, T) -> Unit
) : PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> {

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = object : ReadWriteProperty<Any?, T> {
        private val actualKey = key ?: "${property.name}Key"
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = if (prefs.contains(actualKey)) prefs.getter(actualKey) else fallback
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            prefs.edit {
                if (value == null) {
                    remove(actualKey)
                } else {
                    setter(actualKey, value)
                }
            }
        }
    }
}

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

inline fun <reified T : Enum<T>> SharedPreferences.enumField(key: String? = null): ReadWriteProperty<Any, T?> =
    field<T>(
        key,
        { k, v -> putInt(k, v.ordinal) },
        { k -> getInt(k, -1).let { ord -> if (ord == -1) null else enumValues<T>()[ord] } }
    ).map { this }


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
