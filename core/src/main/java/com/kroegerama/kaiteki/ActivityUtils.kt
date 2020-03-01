package com.kroegerama.kaiteki

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

inline fun <reified T : Activity> Fragment.startActivity(
    options: Bundle? = null,
    block: (Intent.() -> Unit) = {}
) =
    startActivity(Intent(requireContext(), T::class.java).apply(block), options)

inline fun <reified T : Activity> Fragment.startActivityForResult(
    requestCode: Int,
    options: Bundle? = null,
    block: (Intent.() -> Unit) = {}
) =
    startActivityForResult(Intent(requireContext(), T::class.java).apply(block), requestCode, options)

inline fun <reified T : Activity> Context.startActivity(
    options: Bundle? = null,
    block: (Intent.() -> Unit) = {}
) =
    startActivity(Intent(this, T::class.java).apply(block), options)

inline fun <reified T : Activity> Activity.startActivityForResult(
    requestCode: Int,
    options: Bundle? = null,
    block: (Intent.() -> Unit) = {}
) =
    startActivityForResult(Intent(this, T::class.java).apply(block), requestCode, options)

fun Intent.clearTop() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

fun Intent.clearTask() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
}

fun Intent.newTask() {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}