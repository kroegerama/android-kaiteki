package com.kroegerama.kaiteki

import android.view.KeyEvent
import android.widget.EditText

fun EditText.onImeAction(vararg actionIds: Int, block: (actionId: Int, event: KeyEvent?) -> Unit) {
    setOnEditorActionListener { _, actionId: Int, event: KeyEvent? ->
        if (
            (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) ||
            (actionIds.contains(actionId))
        ) {
            block.invoke(actionId, event)
            true
        } else {
            false
        }
    }
}

var EditText.string: String
    get() = text?.toString().orEmpty()
    set(value) = setText(value)