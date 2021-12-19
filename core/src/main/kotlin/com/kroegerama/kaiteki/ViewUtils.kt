package com.kroegerama.kaiteki

import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout

fun View.showIf(value: Boolean, goneIfFalse: Boolean = true) {
    visibility = when {
        value -> View.VISIBLE
        goneIfFalse -> View.GONE
        else -> View.INVISIBLE
    }
}

fun View.showIf(goneIfFalse: Boolean, block: () -> Boolean) {
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

fun View.doOnApplyWindowInsets(
    block: WindowInsetsCompat.(v: View, originalPadding: Insets) -> Unit
) {
    val originalPadding = Insets.of(
        paddingLeft, paddingTop, paddingRight, paddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        insets.block(v, originalPadding)
        insets
    }
    doOnAttach {
        requestApplyInsets()
    }
}

fun View.addWindowInsetsPadding(
    top: Boolean = false,
    bottom: Boolean = false,
    block: ((v: View, originalPadding: Insets) -> Unit)? = null
) = doOnApplyWindowInsets { v, originalPadding ->
    val systemBars = getInsets(WindowInsetsCompat.Type.systemBars())
    v.updatePadding(
        top = originalPadding.top + (systemBars.top.takeIf { top } ?: 0),
        bottom = originalPadding.bottom + (systemBars.bottom.takeIf { bottom } ?: 0)
    )
    block?.invoke(v, originalPadding)
}

fun View.scale(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun ViewPropertyAnimator.scale(scale: Float): ViewPropertyAnimator = scaleX(scale).scaleY(scale)

fun TextInputLayout.clearErrorOnInput() {
    editText!!.doOnTextChanged { _, _, _, _ -> error = null }
}
