package com.kroegerama.kaiteki

import android.graphics.drawable.Drawable
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

operator fun TextView.provideDelegate(value: Any, property: KProperty<*>) = object : ReadWriteProperty<Any, CharSequence?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): CharSequence? = text
    override fun setValue(thisRef: Any, property: KProperty<*>, value: CharSequence?) {
        text = value
    }
}

operator fun ImageView.provideDelegate(value: Any, property: KProperty<*>) = object : ReadWriteProperty<Any, Drawable?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Drawable? = drawable
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Drawable?) = setImageDrawable(value)
}

operator fun CheckBox.provideDelegate(value: Any, property: KProperty<*>) = object : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = isChecked
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        isChecked = value
    }
}

operator fun SwitchCompat.provideDelegate(value: Any, property: KProperty<*>) = object : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = isChecked
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        isChecked = value
    }
}

operator fun ProgressBar.provideDelegate(value: Any, property: KProperty<*>) = object : ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int = progress
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        progress = value
    }
}


