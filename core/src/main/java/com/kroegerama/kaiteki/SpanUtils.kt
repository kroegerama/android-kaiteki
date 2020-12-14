package com.kroegerama.kaiteki

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString

fun TextView.setClickableString(
    pattern: String,
    vararg spans: Pair<String, () -> Unit>
) {
    movementMethod = LinkMovementMethod.getInstance()
    text = getClickableString(pattern, *spans)
}

fun getClickableString(
    pattern: String,
    vararg spans: Pair<String, () -> Unit>
) = buildSpannedString {
    append(pattern)
    spans.forEachIndexed { index, (stringRes, listener) ->
        val search = "?${index + 1}"
        val idx = indexOf(search)
        if (idx == -1) return@forEachIndexed

        val clickable = getClickableSpan(stringRes, listener)
        replace(idx, idx + search.length, clickable)
    }
}

fun getClickableSpan(label: String, listener: () -> Unit) = buildSpannedString {
    append(label)
    setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()
                listener()
            }
        },
        0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}