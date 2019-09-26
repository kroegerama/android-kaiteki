package com.kroegerama.kaiteki

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

inline fun <reified T : Activity> Fragment.startActivity(block: (Intent.() -> Unit) = {}) =
    startActivity(Intent(requireContext(), T::class.java).apply(block))

inline fun <reified T : Activity> Fragment.startActivityForResult(requestCode: Int, block: (Intent.() -> Unit) = {}) =
    startActivityForResult(Intent(requireContext(), T::class.java).apply(block), requestCode)

inline fun <reified T : Activity> Context.startActivity(block: (Intent.() -> Unit) = {}) =
    startActivity(Intent(this, T::class.java).apply(block))

inline fun <reified T : Activity> Activity.startActivityForResult(requestCode: Int, block: (Intent.() -> Unit) = {}) =
    startActivityForResult(Intent(this, T::class.java).apply(block), requestCode)

fun Intent.clearTop() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

fun Intent.clearTask() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
}

fun Intent.newTask() {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}