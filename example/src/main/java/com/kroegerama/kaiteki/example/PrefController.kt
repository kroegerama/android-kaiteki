package com.kroegerama.kaiteki.example

import android.content.SharedPreferences
import com.kroegerama.kaiteki.preference.asField
import com.kroegerama.kaiteki.preference.asFieldNullable

class PrefController(
    private val preferences: SharedPreferences
) {
    fun clear() = preferences.edit().clear().apply()

    var myLongNull: Long? by preferences.asFieldNullable("test")
    var myLong: Long by preferences.asField(0L)

    var myIntNull: Int? by preferences.asFieldNullable()
    var myInt: Int by preferences.asField(0)

    var myStringNull: String? by preferences.asFieldNullable()
    var myString: String by preferences.asField("")

    var myBooleanNull: Boolean? by preferences.asFieldNullable()
    var myBoolean: Boolean by preferences.asField(false)

    var myFloatNull: Float? by preferences.asFieldNullable()
    var myFloat: Float by preferences.asField(0f)

}