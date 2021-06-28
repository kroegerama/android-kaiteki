package com.kroegerama.kaiteki.architecture

import android.content.SharedPreferences
import androidx.lifecycle.LiveData

private class PreferencesLiveData<T>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defValue: T,
    private val getter: (key: String, defValue: T) -> T
) : LiveData<T>() {

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            value = getter(key, defValue)
        }
    }

    override fun onActive() {
        super.onActive()
        value = getter(key, defValue)
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onInactive() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
        super.onInactive()
    }
}

fun SharedPreferences.intLiveData(key: String, defValue: Int): LiveData<Int> {
    return PreferencesLiveData(this, key, defValue, ::getInt)
}

fun SharedPreferences.longLiveData(key: String, defValue: Long): LiveData<Long> {
    return PreferencesLiveData(this, key, defValue, ::getLong)
}

fun SharedPreferences.floatLiveData(key: String, defValue: Float): LiveData<Float> {
    return PreferencesLiveData(this, key, defValue, ::getFloat)
}

fun SharedPreferences.booleanLiveData(key: String, defValue: Boolean): LiveData<Boolean> {
    return PreferencesLiveData(this, key, defValue, ::getBoolean)
}

fun SharedPreferences.stringLiveData(key: String, defValue: String): LiveData<String> {
    return PreferencesLiveData(this, key, defValue) { k, dV -> getString(k, dV) ?: dV }
}
