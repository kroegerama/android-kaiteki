package com.kroegerama.kaiteki

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

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

inline fun <reified T : Fragment> AppCompatActivity.findFragmentById(@IdRes id: Int) =
    supportFragmentManager.findFragmentById(id) as T

inline fun <reified T : Fragment> AppCompatActivity.findFragmentByTag(tag: String) =
    supportFragmentManager.findFragmentByTag(tag) as T

fun AppCompatActivity.launchWhenCreated(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launchWhenCreated(block)
fun AppCompatActivity.launchWhenStarted(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launchWhenStarted(block)
fun AppCompatActivity.launchWhenResumed(block: suspend CoroutineScope.() -> Unit) = lifecycleScope.launchWhenResumed(block)
