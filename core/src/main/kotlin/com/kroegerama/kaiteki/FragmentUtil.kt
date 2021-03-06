package com.kroegerama.kaiteki

import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

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
