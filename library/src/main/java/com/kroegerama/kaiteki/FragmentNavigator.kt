package com.kroegerama.kaiteki

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.kroegerama.kaiteki.baseui.BaseFragment
import java.lang.ref.WeakReference

class FragmentNavigator<Index>(
        manager: FragmentManager,
        provider: FragmentProvider<Index>,
        private val strategy: FragmentStrategy<Index> = FragmentStrategy.ReplaceStrategy()
) {

    interface FragmentProvider<in Index> {

        @get:IdRes
        val fragmentContainer: Int

        fun createFragment(index: Index, payload: Any?): Fragment

        fun decorateTransaction(fromIndex: Index?, toIndex: Index, fragment: Fragment, transaction: FragmentTransaction) {}

        fun onFragmentSelected(index: Index, fragment: Fragment) {}

    }

    private val weakManager = WeakReference(manager)
    private val weakProvider = WeakReference(provider)

    private var currentIndex: Index? = null

    private fun showInternal(newIndex: Index, fragment: Fragment?, payload: Any?, forceCreate: Boolean): Boolean = synchronized(this) {
        val manager = weakManager.get() ?: return@synchronized false
        val provider = weakProvider.get() ?: return@synchronized false

        val oldIndex = currentIndex
        if (oldIndex != null && !strategy.accept(oldIndex, newIndex)) return@synchronized false

        val tag = getTagForIndex(newIndex)
        val oldFrag = getCurrentFragment()
        val previousInstance = manager.findFragmentByTag(tag)

        val newFrag = when {
            fragment != null -> fragment
            forceCreate || strategy.mustCreate(newIndex) -> provider.createFragment(newIndex, payload)
            previousInstance != null && strategy.usePreviousInstance(newIndex, previousInstance) -> previousInstance
            else -> provider.createFragment(newIndex, payload)
        }
        manager.beginTransaction().apply {
            val oldInfo = oldFrag?.let {
                val idx = oldIndex ?: return@let null
                FragmentStrategy.FragmentInfo(it, getTagForIndex(idx))
            }
            val newInfo = FragmentStrategy.FragmentInfo(newFrag, tag)

            (oldFrag as? BaseFragment)?.decorateTransaction(this)
            strategy.handleTransaction(manager, provider, this, oldInfo, newInfo, newFrag !== previousInstance, forceCreate)
            provider.decorateTransaction(oldIndex, newIndex, newFrag, this)
        }.commitNow()
        provider.onFragmentSelected(newIndex, newFrag)
        currentIndex = newIndex
        true
    }

    fun show(index: Index, payload: Any? = null, forceCreate: Boolean = payload != null) = showInternal(index, null, payload, forceCreate)

    fun showFragment(index: Index, fragment: Fragment) = showInternal(index, fragment, null, false)

    fun getCurrentFragment(): Fragment? {
        val manager = weakManager.get() ?: return null
        val idx = currentIndex ?: return null
        return manager.findFragmentByTag(getTagForIndex(idx))
    }

    val selection: Index? get() = currentIndex
    val hasSelection: Boolean get() = currentIndex != null

    private fun getTagForIndex(index: Index) = "${this.javaClass.simpleName}_frag_$index"

    fun handleBackPress(): Boolean {
        val current = (getCurrentFragment() ?: return false) as? BaseFragment ?: return false
        return current.handleBackPress()
    }

    fun saveState(outState: Bundle, indexSaver: (index: Index, key: String, bundle: Bundle) -> Unit) {
        outState.putBundle(STATE_BUNDLE, bundle {
            putBundle(STATE_STRATEGY, Bundle().apply(strategy::saveState))
            indexSaver(currentIndex ?: return@bundle, STATE_SELECTED, this)
        })
    }

    fun loadState(bundle: Bundle, indexLoader: (key: String, bundle: Bundle) -> Index?) {
        val state = bundle.getBundle(STATE_BUNDLE) ?: return
        state.getBundle(STATE_STRATEGY)?.let(strategy::loadState)
        if (state.containsKey(STATE_SELECTED)) {
            currentIndex = indexLoader(STATE_SELECTED, state)
        }
    }

    companion object {
        private const val STATE_BUNDLE = "FragmentProvider"
        private const val STATE_SELECTED = "selected"
        private const val STATE_STRATEGY = "strategy"
    }
}

abstract class FragmentStrategy<Index> {
    data class FragmentInfo(
            val fragment: Fragment,
            val tag: String
    )

    abstract fun accept(fromIndex: Index, toIndex: Index): Boolean
    abstract fun mustCreate(index: Index): Boolean
    abstract fun usePreviousInstance(index: Index, instance: Fragment): Boolean
    abstract fun handleTransaction(
            manager: FragmentManager,
            provider: FragmentNavigator.FragmentProvider<Index>,
            transaction: FragmentTransaction,
            oldInfo: FragmentInfo?,
            newInfo: FragmentInfo,
            isNew: Boolean,
            isForced: Boolean
    )

    open fun saveState(outState: Bundle) {}
    open fun loadState(bundle: Bundle) {}

    open class ReplaceStrategy<Index> : FragmentStrategy<Index>() {
        private val states = HashMap<String, Fragment.SavedState>()

        override fun accept(fromIndex: Index, toIndex: Index) = fromIndex != toIndex
        override fun mustCreate(index: Index) = true
        override fun usePreviousInstance(index: Index, instance: Fragment) = false

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider<Index>,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean,
                isForced: Boolean
        ): Unit = transaction.run {
            if (oldInfo != null) {
                val (oldFrag, oldTag) = oldInfo
                val saveState = manager.saveFragmentInstanceState(oldFrag)
                if (saveState == null) states.remove(oldTag) else states[oldTag] = saveState
                remove(oldFrag)
            }
            val (newFrag, newTag) = newInfo
            val state = states.remove(newTag)
            if (!isForced && state != null) {
                newFrag.setInitialSavedState(state)
            }
            add(provider.fragmentContainer, newFrag, newTag)
        }

        override fun loadState(bundle: Bundle) {
            states.clear()
            val keys = bundle.getStringArray(STATE_KEYS).orEmpty()
            keys.forEach { k ->
                val v: Fragment.SavedState =
                        bundle.getParcelable("$STATE_VALUE_PREFIX.$k") ?: return@forEach
                states[k] = v
            }
        }

        override fun saveState(outState: Bundle) {
            outState.putStringArray(STATE_KEYS, states.keys.toTypedArray())
            states.entries.forEach {
                val (k, v) = it
                outState.putParcelable("$STATE_VALUE_PREFIX.$k", v)
            }
        }

        companion object {
            private const val STATE_KEYS = "state_keys"
            private const val STATE_VALUE_PREFIX = "sate_value"
        }
    }

    open class DetachStrategy<Index> : FragmentStrategy<Index>() {
        override fun accept(fromIndex: Index, toIndex: Index) = fromIndex != toIndex
        override fun mustCreate(index: Index) = false
        override fun usePreviousInstance(index: Index, instance: Fragment) = true

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider<Index>,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean,
                isForced: Boolean
        ): Unit = transaction.run {
            oldInfo?.fragment?.run(::detach)
            val (newFrag, newTag) = newInfo
            if (isNew)
                add(provider.fragmentContainer, newFrag, newTag)
            else
                attach(newFrag)
        }
    }

    open class HideStrategy<Index> : FragmentStrategy<Index>() {
        override fun accept(fromIndex: Index, toIndex: Index) = fromIndex != toIndex
        override fun mustCreate(index: Index) = false
        override fun usePreviousInstance(index: Index, instance: Fragment) = true

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider<Index>,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean,
                isForced: Boolean
        ): Unit = transaction.run {
            oldInfo?.fragment?.run(::hide)
            val (newFrag, newTag) = newInfo
            if (isNew)
                add(provider.fragmentContainer, newFrag, newTag)
            else
                show(newFrag)
        }

    }
}