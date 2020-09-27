package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.FragmentNavigator
import com.kroegerama.kaiteki.FragmentStrategy

abstract class ViewBindingFragmentActivity<Index, VB : ViewBinding>(
    bindingInflater: (LayoutInflater) -> VB,
    protected val startIndex: Index
) : ViewBindingActivity<VB>(bindingInflater), FragmentNavigator.FragmentProvider<Index> {

    protected val navigator by lazy { FragmentNavigator(supportFragmentManager, this, getFragmentStrategy(), getFragmentCommitStrategy()) }

    override fun run() {
        if (!navigator.hasSelection) navigator.show(startIndex)
        super.run()
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

    protected open fun getFragmentStrategy() = FragmentStrategy.ReplaceStrategy<Index>()

    protected open fun getFragmentCommitStrategy() = FragmentStrategy.CommitStrategy.CommitNow

    protected open fun handleBackPress(): Boolean = false

    override fun onBackPressed() {
        if (navigator.handleBackPress() || handleBackPress()) {
            return
        }
        getIndexAfterBackPress(navigator.selection)?.let { idx ->
            navigator.show(idx)
            return
        }

        super.onBackPressed()
    }

}