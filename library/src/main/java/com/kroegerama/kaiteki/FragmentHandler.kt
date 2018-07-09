package com.kroegerama.kaiteki

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.util.Log
import android.util.SparseArray
import com.kroegerama.kaiteki.baseui.BaseFragment


class FragmentHandler(private val manager: FragmentManager, private val provider: FragmentProvider) {

    interface FragmentProvider {

        @get:IdRes
        val fragmentContainer: Int

        fun createFragment(index: Int): BaseFragment

        fun decorateFragmentTransaction(fromIndex: Int, toIndex: Int, fragment: BaseFragment, transaction: FragmentTransaction)

        fun onFragmentSelected(index: Int, fragment: BaseFragment)
    }

    private val states = SparseArray<Fragment.SavedState>()
    private var currentIndex = Integer.MIN_VALUE

    fun showFragment(index: Int): Boolean {
        return showFragment(index, false)
    }

    @Synchronized
    fun showFragment(index: Int, forceCreate: Boolean): Boolean {
        if (index == currentIndex) {
            return false
        }

        val tag = getTagForIndex(index)
        val oldFrag = getCurrentFragment()
        var fragment: BaseFragment? = manager.findFragmentByTag(tag) as BaseFragment?

        val transaction = manager.beginTransaction()
        if (oldFrag != null) {
            oldFrag.decorateTransaction(transaction)

            val savedState = manager.saveFragmentInstanceState(oldFrag)
            states.put(currentIndex, savedState)
        }
        if (forceCreate && fragment != null) {
            transaction.remove(fragment)
            fragment = null
        }

        if (fragment == null) {
            fragment = provider.createFragment(index)
            transaction.add(provider.fragmentContainer, fragment, tag)
        }
        val state = states.get(index)
        if (state != null && !fragment.isRemoving) {
            try {
                fragment.setInitialSavedState(state)
            } catch (e: Exception) {
                Log.w("setInitialSavedState", e)
            }
        }

        provider.decorateFragmentTransaction(currentIndex, index, fragment, transaction)
        transaction
                .replace(provider.fragmentContainer, fragment)
                .commit()

        currentIndex = index

        provider.onFragmentSelected(index, fragment)
        return true
    }

    fun getCurrentFragment(): BaseFragment? {
        return manager.findFragmentByTag(getTagForIndex(currentIndex)) as BaseFragment?
    }

    val selection: Int
        get() = currentIndex

    private fun getTagForIndex(index: Int): String {
        return "tag_$index"
    }

    fun handleBackPress(): Boolean {
        return getCurrentFragment()?.handleBackPress() ?: false
    }

    @SuppressLint("DefaultLocale")
    fun saveState(outState: Bundle) {
        val state = Bundle()

        state.putInt(STATE_COUNT, states.size())
        for (i in 0 until states.size()) {
            state.putInt(String.format(STATE_KEY, i), states.keyAt(i))
            state.putParcelable(String.format(STATE_VALUE, i), states.valueAt(i))
        }
        outState.putBundle(STATE_BUNDLE, state)
    }

    @SuppressLint("DefaultLocale")
    fun loadState(bundle: Bundle) {
        val state = bundle.getBundle(STATE_BUNDLE) ?: return
        val count = state.getInt(STATE_COUNT, 0)
        for (i in 0 until count) {
            val key = state.getInt(String.format(STATE_KEY, i), -1)
            val value = state.getParcelable<Fragment.SavedState>(String.format(STATE_VALUE, i))
            if (key < 0 || value == null) {
                continue
            }
            states.put(key, value)
        }
    }

    private companion object {
        const val STATE_BUNDLE = "FragmentProvider"
        const val STATE_COUNT = "count"
        const val STATE_KEY = "k_%d"
        const val STATE_VALUE = "v_%d"
    }
}