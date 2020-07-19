package com.kroegerama.kaiteki

import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.core.widget.doAfterTextChanged

fun EditText.addClearButton(@DrawableRes clearRes: Int, onClearClick: ((EditText) -> Unit)? = null) {
    val updateFun = {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            0, 0,
            if (text.isNullOrEmpty()) 0 else clearRes,
            0
        )
    }.also { it.invoke() }
    val clearFun = {
        text = null
        updateFun()
        onClearClick?.invoke(this)
        requestFocus()
    }

    doAfterTextChanged { updateFun() }
    var isDown = false
    setOnTouchListener { _, event ->
        val result = if (event.x >= width - totalPaddingRight) {
            if (isDown && event.action == MotionEvent.ACTION_UP) {
                clearFun()
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                isDown = true
            }
            true
        } else false
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDown = false
        }
        result
    }
}

fun EditText.onImeAction(vararg actionIds: Int, block: (actionId: Int, event: KeyEvent) -> Unit) {
    setOnEditorActionListener { _, actionId, event ->
        if (event != null &&
            actionIds.contains(actionId) &&
            event.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_ENTER
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
    set(value) {
        setText(value)
    }