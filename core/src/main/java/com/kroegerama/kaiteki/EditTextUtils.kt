package com.kroegerama.kaiteki

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText

fun EditText.onImeAction(vararg actionIds: Int, block: (actionId: Int, event: KeyEvent?, text: CharSequence) -> Unit) {
    setOnEditorActionListener { _, actionId: Int, event: KeyEvent? ->
        if (
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) ||
            (actionIds.contains(actionId))
        ) {
            block.invoke(actionId, event, text)
            true
        } else {
            false
        }
    }
}

fun EditText.onImeActionDone(listener: (CharSequence) -> Unit) = onImeAction(EditorInfo.IME_ACTION_DONE) { _, _, text -> listener(text) }
fun EditText.onImeActionSend(listener: (CharSequence) -> Unit) = onImeAction(EditorInfo.IME_ACTION_SEND) { _, _, text -> listener(text) }
fun EditText.onImeActionSearch(listener: (CharSequence) -> Unit) = onImeAction(EditorInfo.IME_ACTION_SEARCH) { _, _, text -> listener(text) }
fun EditText.onImeActionGo(listener: (CharSequence) -> Unit) = onImeAction(EditorInfo.IME_ACTION_GO) { _, _, text -> listener(text) }

var EditText.string: String
    get() = text?.toString().orEmpty()
    set(value) = setText(value)