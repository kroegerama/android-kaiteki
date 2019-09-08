package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import com.kroegerama.kaiteki.FragmentNavigator

abstract class BaseFragmentActivity<Index>(
        @LayoutRes layout: Int,
        @IdRes override val fragmentContainer: Int,
        protected val startIndex: Index,
        @MenuRes optionsMenu: Int = 0
) : BaseActivity(layout, optionsMenu), FragmentNavigator.FragmentProvider<Index> {

    protected val navigator by lazy { FragmentNavigator(supportFragmentManager, this) }

    override fun run(runState: RunState) {
        if (!navigator.hasSelection) navigator.show(startIndex)
        super.run(runState)
    }

    override fun saveState(outState: Bundle) {
        navigator.saveState(outState, ::saveIndexState)
        super.saveState(outState)
    }

    override fun loadState(state: Bundle) {
        navigator.loadState(state, ::loadIndexState)
        super.loadState(state)
    }

    protected abstract fun saveIndexState(index: Index, key: String, bundle: Bundle)

    protected abstract fun loadIndexState(key: String, bundle: Bundle): Index?

}