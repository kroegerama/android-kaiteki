package com.kroegerama.kaiteki.example

import android.content.SharedPreferences
import com.kroegerama.kaiteki.booleanField
import com.kroegerama.kaiteki.enumField
import com.kroegerama.kaiteki.floatField
import com.kroegerama.kaiteki.intField
import com.kroegerama.kaiteki.longField
import com.kroegerama.kaiteki.stringField
import com.kroegerama.kaiteki.stringSetField

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

    enum class TestEnum {
        One, Two, Three;
        val next get() = when(this){
            One -> Two
            Two -> Three
            Three -> One
        }
    }

}