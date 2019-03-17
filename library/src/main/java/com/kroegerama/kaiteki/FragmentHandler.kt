package com.kroegerama.kaiteki

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.kroegerama.kaiteki.baseui.BaseFragment


class FragmentHandler(private val manager: FragmentManager, private val provider: FragmentProvider) {

    interface FragmentProvider {

        @get:IdRes
        val fragmentContainer: Int

        fun createFragment(index: Int, payload: Any?): Fragment

        fun decorateFragmentTransaction(fromIndex: Int, toIndex: Int, fragment: Fragment, transaction: FragmentTransaction)

        fun onFragmentSelected(index: Int, fragment: Fragment)
    }

    private val states = SparseArray<Fragment.SavedState>()
    private var currentIndex = Integer.MIN_VALUE

    @Synchronized
    fun showFragment(index: Int, forceCreate: Boolean = false, payload: Any? = null): Boolean {
        if (index == currentIndex && !forceCreate) {
            return false
        }

        val tag = getTagForIndex(index)
        val oldFrag = getCurrentFragment()
        var fragment: Fragment? = manager.findFragmentByTag(tag)

        val transaction = manager.beginTransaction()
        if (oldFrag != null) {
            (oldFrag as? BaseFragment)?.decorateTransaction(transaction)

            val savedState = manager.saveFragmentInstanceState(oldFrag)
            states.put(currentIndex, savedState)
        }
        if (forceCreate && fragment != null) {
            transaction.remove(fragment)
            fragment = null
        }

        if (fragment == null) {
            fragment = provider.createFragment(index, payload)
            transaction.add(provider.fragmentContainer, fragment, tag)
        }
        val state = states.get(index)
        if (state != null && !fragment.isRemoving) {
            try {
                fragment.setInitialSavedState(state)
            } catch (e: Exception) {
                Log.w("setInitialSavedState", e)
            }
            states.remove(index)
        }

        provider.decorateFragmentTransaction(currentIndex, index, fragment, transaction)
        transaction
                .replace(provider.fragmentContainer, fragment)
                .commit()

        currentIndex = index

        provider.onFragmentSelected(index, fragment)
        return true
    }

    @Synchronized
    fun removeCurrentFragment() {
        val oldFrag = getCurrentFragment() ?: return
        manager.beginTransaction().apply {
            (oldFrag as? BaseFragment)?.decorateTransaction(this)
            remove(oldFrag)
            commit()
        }
        states.remove(currentIndex)
        currentIndex = Int.MIN_VALUE
    }

    fun getCurrentFragment(): Fragment? {
        return manager.findFragmentByTag(getTagForIndex(currentIndex))
    }

    val selection: Int
        get() = currentIndex

    private fun getTagForIndex(index: Int): String {
        return "tag_$index"
    }

    fun handleBackPress(): Boolean {
        val current = (getCurrentFragment() ?: return false) as? BaseFragment ?: return false
        return current.handleBackPress()
    }

    @SuppressLint("DefaultLocale")
    fun saveState(outState: Bundle) {
        outState.putBundle(STATE_BUNDLE, bundle {
            putInt(STATE_SELECTED, currentIndex)

            putInt(STATE_COUNT, states.size())
            for (i in 0 until states.size()) {
                putInt(String.format(STATE_KEY, i), states.keyAt(i))
                putParcelable(String.format(STATE_VALUE, i), states.valueAt(i))
            }
        })
    }

    @SuppressLint("DefaultLocale")
    fun loadState(bundle: Bundle) {
        val state = bundle.getBundle(STATE_BUNDLE) ?: return

        currentIndex = state.getInt(STATE_SELECTED, currentIndex)
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
        const val STATE_SELECTED = "selected"
    }
}