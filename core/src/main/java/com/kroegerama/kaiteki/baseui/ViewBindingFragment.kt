package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.*
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.FragmentNavigator

abstract class ViewBindingFragment<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment(), FragmentNavigator.BaseFragment {

    @StringRes
    open val title: Int = 0

    @MenuRes
    protected open val optionsMenu: Int = 0

    private var _binding: VB? = null

    protected val binding get() = _binding!!

    protected inline fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        bindingInflater(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (optionsMenu != 0) {
            setHasOptionsMenu(true)
        }
        binding.setupGUI()
        savedInstanceState?.let(::loadState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        run()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (optionsMenu != 0) {
            inflater.inflate(optionsMenu, menu)
        }
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