package com.kroegerama.kaiteki.baseui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

@Deprecated("Use ViewBindingMaterialDialogFragment instead", ReplaceWith("ViewBindingMaterialDialogFragment<VB>"))
abstract class ViewBindingDialogFragment<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    @StyleRes
    protected open val dialogTheme: Int = 0

    private var _binding: VB? = null

    protected val binding get() = _binding!!

    protected inline fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prepare()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        if (dialogTheme == 0)
            super.onCreateDialog(savedInstanceState)
        else
            Dialog(requireContext(), dialogTheme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        bindingInflater(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupGUI()
        savedInstanceState?.let(::loadState)
    }

    @Deprecated("Deprecated in Java")
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

    protected open fun prepare() {}

    protected open fun VB.setupGUI() {}

    protected open fun run() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected fun window(block: Window.() -> Unit) = dialog?.window?.apply(block)
}