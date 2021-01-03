package com.kroegerama.kaiteki

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar


fun View.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) =
    Snackbar.make(this, message, duration).apply {
        apply(block)
        show()
    }

fun View.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) =
    Snackbar.make(this, message, duration).apply {
        apply(block)
        show()
    }

fun Activity.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) =
    findViewById<View>(android.R.id.content).snackBar(message, duration, block)

fun Activity.snackBar(
    message: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    block: Snackbar.() -> Unit = {}
) = findViewById<View>(android.R.id.content).snackBar(message, duration, block)

fun Fragment.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) =
    requireView().snackBar(message, duration, block)

fun Fragment.snackBar(
    message: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    block: Snackbar.() -> Unit = {}
) = requireView().snackBar(message, duration, block)

fun Snackbar.doOnDismiss(block: (event: Int) -> Unit) {
    addCallback(object : Snackbar.Callback() {
        override fun onDismissed(transientBottomBar: Snackbar, event: Int) = block(event)
    })
}