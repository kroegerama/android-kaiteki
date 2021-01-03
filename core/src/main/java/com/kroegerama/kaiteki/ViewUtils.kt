package com.kroegerama.kaiteki

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout

val Context.selectableItemBackground
    @DrawableRes
    get() =
        obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).run {
            val backgroundResource = getResourceId(0, 0)
            recycle()
            backgroundResource
        }

val Context.selectableItemBackgroundBorderless
    @DrawableRes
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    get() =
        obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackgroundBorderless)).run {
            val backgroundResource = getResourceId(0, 0)
            recycle()
            backgroundResource
        }

val Context.listPreferredItemHeight
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeight)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

val Context.listPreferredItemHeightSmall
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeightSmall)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

val Context.listPreferredItemHeightLarge
    @DimenRes
    get() = obtainStyledAttributes(intArrayOf(android.R.attr.listPreferredItemHeightLarge)).run {
        val sizeRes = getResourceId(0, 0)
        recycle()
        sizeRes
    }

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

fun TextInputLayout.clearErrorOnInput() {
    editText!!.doOnTextChanged { _, _, _, _ -> error = null }
}
