package com.kroegerama.kaiteki

import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

fun Fragment.startActivity(cls: Class<*>, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(requireContext(), cls)
    block?.invoke(intent)
    startActivity(intent)
}

fun Fragment.startActivityForResult(cls: Class<*>, requestCode: Int, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(requireContext(), cls)
    block?.invoke(intent)
    startActivityForResult(intent, requestCode)
}

fun Context.startActivity(cls: Class<*>, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, cls)
    block?.invoke(intent)
    startActivity(intent)
}

fun AppCompatActivity.startActivityForResult(cls: Class<*>, requestCode: Int, block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, cls)
    block?.invoke(intent)
    startActivityForResult(intent, requestCode)
}

fun Intent.clearTop() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

fun Intent.clearTask() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
}

fun Intent.newTask() {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}