package com.kroegerama.kaiteki

import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout

fun View.showIf(value: Boolean, goneIfFalse: Boolean = true) {
    visibility = when {
        value -> View.VISIBLE
        goneIfFalse -> View.GONE
        else -> View.INVISIBLE
    }
}

fun View.showIf(goneIfFalse: Boolean = true, block: () -> Boolean) {
    visibility = when {
        block() -> View.VISIBLE
        goneIfFalse -> View.GONE
        else -> View.INVISIBLE
    }
}

inline fun <reified T : View> T.onClick(crossinline block: T.() -> Unit) {
    setOnClickListener {
        block(this)
    }
}

inline fun <reified T : View> T.onLongClick(crossinline block: T.() -> Boolean) {
    setOnLongClickListener {
        block(this)
    }
}

fun View.scale(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun ViewPropertyAnimator.scale(scale: Float): ViewPropertyAnimator = scaleX(scale).scaleY(scale)

fun TextInputLayout.clearError() {
    error = null
    isErrorEnabled = false
}

fun TextInputLayout.clearErrorOnInput() {
    editText!!.doOnTextChanged { _, _, _, _ ->
        clearError()
    }
    setErrorIconOnClickListener {
        clearError()
    }
}

fun <T : TextInputLayout> Collection<T>.clearErrorOnInput() = forEach { til ->
    til.clearErrorOnInput()
}

var TextView.textAndVisibility: CharSequence?
    get() = text
    set(value) {
        text = value
        isVisible = !text.isNullOrBlank()
    }
