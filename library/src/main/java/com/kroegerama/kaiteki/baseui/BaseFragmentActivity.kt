package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import com.kroegerama.kaiteki.FragmentNavigator
import com.kroegerama.kaiteki.FragmentStrategy

abstract class BaseFragmentActivity<Index>(
    @LayoutRes layout: Int,
    @IdRes override val fragmentContainer: Int,
    protected val startIndex: Index,
    @MenuRes optionsMenu: Int = 0,
    strategy: FragmentStrategy<Index> = FragmentStrategy.ReplaceStrategy(),
    commitStrategy: FragmentStrategy.CommitStrategy = FragmentStrategy.CommitStrategy.CommitNow
) : BaseActivity(layout, optionsMenu), FragmentNavigator.FragmentProvider<Index> {

    protected val navigator by lazy { FragmentNavigator(supportFragmentManager, this, strategy, commitStrategy) }

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

    fun navigate(index: Index, payload: Any? = null, forceCreate: Boolean = payload != null) = navigator.show(index, payload, forceCreate)

    protected abstract fun saveIndexState(index: Index, key: String, bundle: Bundle)

    protected abstract fun loadIndexState(key: String, bundle: Bundle): Index?

    protected open fun getIndexAfterBackPress(currentIndex: Index?): Index? = if (currentIndex != startIndex) startIndex else null

    protected open fun handleBackPress(): Boolean = false

    override fun onBackPressed() {
        if (navigator.handleBackPress() || handleBackPress()) {
            return
        }
        getIndexAfterBackPress(navigator.selection)?.let { idx ->
            if (idx != null) {
                navigator.show(idx)
                return
            }
        }

        super.onBackPressed()
    }

}