package com.kroegerama.kaiteki.example

import android.content.SharedPreferences
import com.kroegerama.kaiteki.*
import com.kroegerama.kaiteki.architecture.intFlow

class PrefController(
    private val preferences: SharedPreferences
) {

    var myNewLong by preferences.longField()
    var myNewInt by preferences.intField()
    var myNewString by preferences.stringField()
    var myNewStringSet by preferences.stringSetField()
    var myNewBoolean by preferences.booleanField()
    var myNewFloat by preferences.floatField()
    var myNewEnum by preferences.enumField<TestEnum>()

    val intFlow = preferences.intFlow("myNewIntKey")

    enum class TestEnum {
        One, Two, Three;

        val next
            get() = when (this) {
                One -> Two
                Two -> Three
                Three -> One
            }
    }

}