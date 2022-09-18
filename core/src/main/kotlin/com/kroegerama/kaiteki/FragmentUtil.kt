package com.kroegerama.kaiteki

import android.os.Build
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import java.io.Serializable

inline fun <reified T> Fragment.callFirstListener(crossinline block: T.() -> Unit): Boolean {
    (targetFragment as? T)?.let {
        block(it)
        return true
    }
    var parent: Fragment? = parentFragment
    while (parent != null) {
        (parent as? T)?.let {
            block(it)
            return true
        }
        parent = parent.parentFragment
    }
    (activity as? T)?.let {
        block(it)
        return true
    }
    (context as? T)?.let {
        block(it)
        return true
    }
    return false
}

fun FragmentManager.removeByTag(tag: String) {
    findFragmentByTag(tag)?.let { beginTransaction().remove(it).commit() }
}

fun Fragment.postponeUntilPreDraw(target: View = requireView()) {
    postponeEnterTransition()
    target.doOnPreDraw {
        startPostponedEnterTransition()
    }
}

fun Fragment.postponeUntilLayout(target: View = requireView()) {
    postponeEnterTransition()
    target.doOnLayout {
        startPostponedEnterTransition()
    }
}

val Fragment.viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope
fun Fragment.launchWhenViewCreated(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenCreated(block)
fun Fragment.launchWhenViewStarted(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenStarted(block)
fun Fragment.launchWhenViewResumed(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenResumed(block)

fun <T> Fragment.collectLatestWithViewLifecycle(flow: Flow<T>, action: suspend (value: T) -> Unit) = launchWhenViewCreated {
    flow.flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest(action)
}

inline fun <reified T : Serializable?> Fragment.argument(key: String, defaultValue: T): Lazy<T> = lazy {
    requireArguments().run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(key, T::class.java) ?: defaultValue
        } else {
            (getSerializable(key) as? T) ?: defaultValue
        }
    }
}

inline fun <reified T : Serializable?> Fragment.argument(key: String) = argument<T?>(key, null)
