package com.kroegerama.kaiteki.architecture

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private fun <T> SharedPreferences.flow(
    key: String,
    getter: () -> T
): Flow<T?> = callbackFlow {
    fun getOrNull() = if (contains(key)) getter() else null

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, callbackKey ->
        Log.d("PrefsFlow", "KEY $callbackKey")
        if (callbackKey == key) {
            trySendBlocking(getOrNull())
        }
    }
    trySend(getOrNull())
    registerOnSharedPreferenceChangeListener(listener)
    awaitClose {
        unregisterOnSharedPreferenceChangeListener(listener)
    }
}

fun SharedPreferences.intFlow(key: String) = flow(key) { getInt(key, 0) }

fun SharedPreferences.longFlow(key: String) = flow(key) { getLong(key, 0L) }

fun SharedPreferences.floatFlow(key: String) = flow(key) { getFloat(key, 0f) }

fun SharedPreferences.booleanFlow(key: String) = flow(key) { getBoolean(key, false) }

fun SharedPreferences.stringFlow(key: String) = flow(key) { getString(key, null) }
