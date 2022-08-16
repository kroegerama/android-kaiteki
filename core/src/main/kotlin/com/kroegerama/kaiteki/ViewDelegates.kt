package com.kroegerama.kaiteki

import android.graphics.drawable.Drawable
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

operator fun TextView.provideDelegate(value: Any, property: KProperty<*>): ReadWriteProperty<Any?, CharSequence?> =
    ViewDelegateProperty(::getText, ::setText)

operator fun ImageView.provideDelegate(value: Any, property: KProperty<*>): ReadWriteProperty<Any?, Drawable?> =
    ViewDelegateProperty(::getDrawable, ::setImageDrawable)

operator fun CompoundButton.provideDelegate(value: Any, property: KProperty<*>): ReadWriteProperty<Any?, Boolean> =
    ViewDelegateProperty(::isChecked, ::setChecked)

operator fun ProgressBar.provideDelegate(value: Any, property: KProperty<*>): ReadWriteProperty<Any?, Int> =
    ViewDelegateProperty(::getProgress, ::setProgress)

internal class ViewDelegateProperty<T>(
    private val getter: () -> T,
    private val setter: (T) -> Unit
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setter(value)
}
