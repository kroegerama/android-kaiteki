package com.kroegerama.kaiteki

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

inline fun <reified T : Activity> Fragment.startActivity(
    options: Bundle? = null,
    crossinline block: (Intent.() -> Unit) = {}
) = startActivity(Intent(requireContext(), T::class.java).apply(block), options)

inline fun <reified T : Activity> Context.startActivity(
    options: Bundle? = null,
    crossinline block: (Intent.() -> Unit) = {}
) = startActivity(Intent(this, T::class.java).apply(block), options)

inline fun Fragment.registerStartActivityForResult(
    crossinline callback: ActivityResult.() -> Unit
) = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { callback.invoke(it) }

inline fun AppCompatActivity.registerStartActivityForResult(
    crossinline callback: ActivityResult.() -> Unit
) = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { callback.invoke(it) }

inline fun <reified T : Activity> intentOf(
    context: Context,
    crossinline block: Intent.() -> Unit = {}
) = Intent(context, T::class.java).apply(block)

inline fun <reified T : Activity> ActivityResultLauncher<Intent>.launch(
    context: Context,
    options: ActivityOptionsCompat? = null,
    crossinline block: Intent.() -> Unit = {}
) = launch(intentOf<T>(context, block), options)

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
