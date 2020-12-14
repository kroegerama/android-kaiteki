package com.kroegerama.kaiteki.baseui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.kroegerama.kaiteki.FragmentNavigator

abstract class BaseFragment(
    @LayoutRes protected val layout: Int,
    @StringRes val title: Int = 0,
    @MenuRes protected val optionsMenu: Int = 0
) : Fragment(), FragmentNavigator.BaseFragment {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()
        arguments?.let(::handleArguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (optionsMenu != 0) {
            setHasOptionsMenu(true)
        }
        setupGUI()

        savedInstanceState?.let(::loadState)
    }

    override fun onStart() {
        super.onStart()
        run()
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

    protected open fun setupGUI() {}

    protected open fun run() {}

    protected open fun handleArguments(args: Bundle) {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    override fun handleBackPress(): Boolean {
        return false
    }

    override fun decorateTransaction(transaction: FragmentTransaction) {}
}