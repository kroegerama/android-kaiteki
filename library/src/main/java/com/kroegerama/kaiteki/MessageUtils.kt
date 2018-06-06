package com.kroegerama.kaiteki

import android.content.Context
import android.os.SystemClock
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.util.SparseArray
import android.view.View
import android.widget.Toast

inline fun View.snackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) {
    val sb = Snackbar.make(this, message, duration)
    sb.apply(block)
    sb.show()
}

inline fun View.snackBar(@StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT, block: Snackbar.() -> Unit = {}) {
    val sb = Snackbar.make(this, message, duration)
    sb.apply(block)
    sb.show()
}

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.toastDebounced(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    toastDebouncer.triggerMessage(message.hashCode()) { toast(message, duration) }
}

fun Context.toastDebounced(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    toastDebouncer.triggerMessage(message) { toast(message, duration) }
}

private val toastDebouncer = Debouncer()

class Debouncer(private val debounceTime: Int = DEFAULT_DEBOUNCE_TIME) {
    private val messages: SparseArray<MessageContainer> = SparseArray()

    fun register(id: Int, block: () -> Unit) {
        registerInternal(id, block)
    }

    private fun registerInternal(id: Int, block: () -> Unit): MessageContainer {
        val container = MessageContainer(block)
        messages.put(id, container)
        return container
    }

    fun triggerMessage(id: Int, fallback: (() -> Unit)?) {
        messages.get(id)?.trigger() ?: fallback?.let { registerInternal(id, it).trigger() }
    }

    private inner class MessageContainer(private val block: () -> Unit) {
        private var lastMessage: Long = 0

        fun trigger(): Boolean {
            if (SystemClock.elapsedRealtime() < lastMessage + debounceTime) {
                return false
            }
            block.invoke()
            lastMessage = SystemClock.elapsedRealtime()
            return true
        }
    }

    companion object {
        private const val DEFAULT_DEBOUNCE_TIME: Int = 5000
    }
}