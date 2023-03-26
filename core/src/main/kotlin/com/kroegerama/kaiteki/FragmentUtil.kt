package com.kroegerama.kaiteki

import android.os.Build
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kroegerama.kaiteki.architecture.launchAndCollectLatestWithLifecycleState
import com.kroegerama.kaiteki.architecture.launchAndCollectWithLifecycleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable

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

fun <T> Fragment.launchAndCollectLatestWithViewLifecycleState(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend (T) -> Unit
) = flow.launchAndCollectLatestWithLifecycleState(viewLifecycleOwner, state, block)

fun <T> Fragment.launchAndCollectWithViewLifecycleState(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend (T) -> Unit
) = flow.launchAndCollectWithLifecycleState(viewLifecycleOwner, state, block)

fun Fragment.launchAndRepeatOnViewLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state, block)
    }
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

inline fun <reified T : Fragment> Fragment.requireParentFragmentWithType(): T {
    var currentFrag = this
    while (currentFrag !is T) {
        currentFrag = currentFrag.requireParentFragment()
    }
    return currentFrag
}

@Deprecated("see LifecycleCoroutineScope", ReplaceWith("viewLifecycleScope.launchWhenCreated(block)"))
fun Fragment.launchWhenViewCreated(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenCreated(block)

@Deprecated("see LifecycleCoroutineScope", ReplaceWith("viewLifecycleScope.launchWhenStarted(block)"))
fun Fragment.launchWhenViewStarted(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenStarted(block)

@Deprecated("see LifecycleCoroutineScope", ReplaceWith("viewLifecycleScope.launchWhenResumed(block)"))
fun Fragment.launchWhenViewResumed(block: suspend CoroutineScope.() -> Unit) = viewLifecycleScope.launchWhenResumed(block)

@Deprecated(
    "use `launchAndCollectOnViewLifecycleState` instead",
    replaceWith = ReplaceWith("launchAndCollectOnViewLifecycleState(flow) { action(it) }")
)
fun <T> Fragment.collectLatestWithViewLifecycle(flow: Flow<T>, action: suspend (value: T) -> Unit) = launchWhenViewCreated {
    flow.flowWithLifecycle(viewLifecycleOwner.lifecycle).collectLatest(action)
}

@Deprecated("Use fragmentResultListener instead")
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
