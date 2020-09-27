package com.kroegerama.kaiteki

import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

inline fun <reified T> Fragment.notifyListener(block: T.() -> Unit) {
    (targetFragment as? T)?.let {
        block(it)
        return
    }
    var parent: Fragment? = parentFragment
    while (parent != null) {
        (parent as? T)?.let {
            block(it)
            return
        }
        parent = parent.parentFragment
    }
    (activity as? T)?.let {
        block(it)
        return
    }
    (context as? T)?.let {
        block(it)
        return
    }
    throw IllegalStateException("No implementation found for ${T::class.java.name}")
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