package com.kroegerama.kaiteki

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.kroegerama.kaiteki.baseui.BaseFragment
import java.lang.ref.WeakReference

class FragmentNavigator(
        manager: FragmentManager,
        provider: FragmentProvider,
        private val strategy: FragmentStrategy = FragmentStrategy.ReplaceStrategy()
) {

    interface FragmentProvider {

        @get:IdRes
        val fragmentContainer: Int

        fun createFragment(index: Int, payload: Any?): Fragment

        fun decorateTransaction(fromIndex: Int, toIndex: Int, fragment: Fragment, transaction: FragmentTransaction) {}

        fun onFragmentSelected(index: Int, fragment: Fragment) {}

    }

    private val weakManager = WeakReference(manager)
    private val weakProvider = WeakReference(provider)

    private var currentIndex = INDEX_UNKNOWN

    private fun showInternal(newIndex: Int, fragment: Fragment?, payload: Any?): Boolean = synchronized(this) {
        val manager = weakManager.get() ?: return@synchronized false
        val provider = weakProvider.get() ?: return@synchronized false

        val oldIndex = currentIndex
        if (!strategy.accept(oldIndex, newIndex)) return@synchronized false

        val tag = getTagForIndex(newIndex)
        val oldFrag = getCurrentFragment()
        val previousInstance = manager.findFragmentByTag(tag)

        val newFrag = when {
            fragment != null -> fragment
            strategy.mustCreate(newIndex) -> provider.createFragment(newIndex, payload)
            previousInstance != null && strategy.usePreviousInstance(newIndex, previousInstance) -> previousInstance
            else -> provider.createFragment(newIndex, payload)
        }
        manager.beginTransaction().apply {
            val oldInfo = oldFrag?.let { FragmentStrategy.FragmentInfo(it, getTagForIndex(oldIndex)) }
            val newInfo = FragmentStrategy.FragmentInfo(newFrag, tag)

            (oldFrag as? BaseFragment)?.decorateTransaction(this)
            strategy.handleTransaction(manager, provider, this, oldInfo, newInfo, newFrag !== previousInstance)
            provider.decorateTransaction(oldIndex, newIndex, newFrag, this)
        }.commitNow()
        provider.onFragmentSelected(newIndex, newFrag)
        currentIndex = newIndex
        true
    }

    fun show(index: Int, payload: Any? = null) = showInternal(index, null, payload)

    fun showFragment(index: Int, fragment: Fragment) = showInternal(index, fragment, null)

    fun getCurrentFragment(): Fragment? {
        val manager = weakManager.get() ?: return null
        return manager.findFragmentByTag(getTagForIndex(currentIndex))
    }

    val selection: Int get() = currentIndex
    val hasSelection: Boolean get() = currentIndex != INDEX_UNKNOWN

    private fun getTagForIndex(index: Int) = "${this.javaClass.simpleName}_frag_$index"

    fun handleBackPress(): Boolean {
        val current = (getCurrentFragment() ?: return false) as? BaseFragment ?: return false
        return current.handleBackPress()
    }

    fun saveState(outState: Bundle) {
        outState.putBundle(STATE_BUNDLE, bundle {
            putInt(STATE_SELECTED, currentIndex)
            putBundle(STATE_STRATEGY, Bundle().apply(strategy::saveState))
        })
    }

    fun loadState(bundle: Bundle) {
        val state = bundle.getBundle(STATE_BUNDLE) ?: return
        currentIndex = state.getInt(STATE_SELECTED, currentIndex)
        val strategyBundle = state.getBundle(STATE_STRATEGY) ?: return
        strategy.loadState(strategyBundle)
    }

    companion object {
        private const val STATE_BUNDLE = "FragmentProvider"
        private const val STATE_SELECTED = "selected"
        private const val STATE_STRATEGY = "strategy"

        const val INDEX_UNKNOWN = Int.MIN_VALUE
    }
}

abstract class FragmentStrategy {
    data class FragmentInfo(
            val fragment: Fragment,
            val tag: String
    )

    internal abstract fun accept(fromIndex: Int, toIndex: Int): Boolean
    internal abstract fun mustCreate(index: Int): Boolean
    internal abstract fun usePreviousInstance(index: Int, instance: Fragment): Boolean
    internal abstract fun handleTransaction(
            manager: FragmentManager,
            provider: FragmentNavigator.FragmentProvider,
            transaction: FragmentTransaction,
            oldInfo: FragmentInfo?,
            newInfo: FragmentInfo,
            isNew: Boolean
    )

    internal open fun saveState(outState: Bundle) {}
    internal open fun loadState(bundle: Bundle) {}

    class ReplaceStrategy : FragmentStrategy() {
        private val states = HashMap<String, Fragment.SavedState>()

        override fun accept(fromIndex: Int, toIndex: Int) = fromIndex != toIndex
        override fun mustCreate(index: Int) = true
        override fun usePreviousInstance(index: Int, instance: Fragment) = false

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean
        ): Unit = transaction.run {
            if (oldInfo != null) {
                val (oldFrag, oldTag) = oldInfo
                val saveState = manager.saveFragmentInstanceState(oldFrag)
                if (saveState == null) states.remove(oldTag) else states[oldTag] = saveState
                remove(oldFrag)
            }
            val (newFrag, newTag) = newInfo
            val state = states[newTag]
            if (state != null) {
                newFrag.setInitialSavedState(state)
            }
            add(provider.fragmentContainer, newFrag, newTag)
        }
    }

    object DetachStrategy : FragmentStrategy() {
        override fun accept(fromIndex: Int, toIndex: Int) = fromIndex != toIndex
        override fun mustCreate(index: Int) = false
        override fun usePreviousInstance(index: Int, instance: Fragment) = true

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean
        ): Unit = transaction.run {
            oldInfo?.fragment?.run(::detach)
            val (newFrag, newTag) = newInfo
            if (isNew)
                add(provider.fragmentContainer, newFrag, newTag)
            else
                attach(newFrag)
        }
    }

    object HideStrategy : FragmentStrategy() {
        override fun accept(fromIndex: Int, toIndex: Int) = fromIndex != toIndex
        override fun mustCreate(index: Int) = false
        override fun usePreviousInstance(index: Int, instance: Fragment) = true

        override fun handleTransaction(
                manager: FragmentManager,
                provider: FragmentNavigator.FragmentProvider,
                transaction: FragmentTransaction,
                oldInfo: FragmentInfo?,
                newInfo: FragmentInfo,
                isNew: Boolean
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