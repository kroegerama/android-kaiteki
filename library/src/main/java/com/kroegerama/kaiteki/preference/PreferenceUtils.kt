package com.kroegerama.kaiteki.preference

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty

inline fun <reified T : Any> SharedPreferences.asField(default: T, key: String? = null):
        ReadWriteProperty<Any, T> =
    PreferenceFieldBinder(this, default, key)

inline fun <reified T : Any?> SharedPreferences.asFieldNullable(key: String? = null):
        ReadWriteProperty<Any, T?> =
    PreferenceFieldBinderNullable(this, key)