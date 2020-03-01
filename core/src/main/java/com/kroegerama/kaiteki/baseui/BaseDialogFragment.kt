package com.kroegerama.kaiteki.baseui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment(
    @LayoutRes protected val layout: Int,
    @StyleRes protected val dialogTheme: Int = 0
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        if (dialogTheme == 0)
            super.onCreateDialog(savedInstanceState)
        else
            Dialog(requireContext(), dialogTheme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layout, container, false).also {
            prepare()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let(::loadState)
        setupGUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onStart() {
        super.onStart()
        run()
    }

    protected open fun prepare() {}

    protected open fun setupGUI() {}

    protected open fun run() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected fun window(block: Window.() -> Unit) = dialog?.window?.apply(block)
}