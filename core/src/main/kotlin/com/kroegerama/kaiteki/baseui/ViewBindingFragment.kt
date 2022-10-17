package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.FragmentNavigator
import com.kroegerama.kaiteki.MenuProviderOwner
import com.kroegerama.kaiteki.findMenuHost

abstract class ViewBindingFragment<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment(), FragmentNavigator.BaseFragment {

    @StringRes
    open val title: Int = 0

    private var _binding: VB? = null

    protected val binding get() = _binding!!

    protected inline fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        prepare()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        bindingInflater(inflater, container, false).also { _binding = it }.root

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (this) {
            is MenuProviderOwner -> findMenuHost()?.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.STARTED)
            is MenuProvider -> findMenuHost()?.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        }
        binding.setupGUI()
        savedInstanceState?.let(::loadState)
        run()
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    protected open fun prepare() {}

    protected open fun VB.setupGUI() {}

    protected open fun run() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    override fun handleBackPress(): Boolean {
        return false
    }

    override fun decorateTransaction(transaction: FragmentTransaction) {}
}