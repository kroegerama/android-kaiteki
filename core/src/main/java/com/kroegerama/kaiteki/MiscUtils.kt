package com.kroegerama.kaiteki

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlin.math.ln
import kotlin.math.pow

fun bundle(block: Bundle.() -> Unit) = Bundle().apply(block)

fun String?.nullIfBlank() = if (isNullOrBlank()) null else this

fun View.closeKeyboard() {
    clearFocus()
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.requestInputFocus() {
    requestFocusFromTouch()
    context.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
}

fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Long.asHumanReadableBytes(si: Boolean = false): String {
    val unit = if (si) 1000f else 1024f
    if (this < unit) return "$this B"
    val exp = (ln(toDouble()) / ln(unit.toDouble())).toInt()

    //Long.MAX_VALUE -> 9,2 EB / 8,0 EiB
    val chars = if (si) "kMGTPEZY" else "KMGTPEZY"
    val pre = chars[exp - 1] + if (si) "" else "i"
    val value = this / unit.pow(exp.toFloat())
    return "%.1f %sB".format(value, pre)
}

inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}

inline fun tryIgnore(block: () -> Unit) = try {
    block()
} catch (e: Exception) {
    //ignore
}