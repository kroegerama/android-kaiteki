package com.kroegerama.kaiteki.baseui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StyleRes
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class ViewBindingBottomSheetDialogFragment<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
    @StyleRes protected val dialogTheme: Int = 0
) : BottomSheetDialogFragment() {

    private var binding: VB? = null

    protected fun binding(block: VB.() -> Unit) {
        binding!!.apply(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        if (dialogTheme == 0)
            super.onCreateDialog(savedInstanceState)
        else
            BottomSheetDialog(requireContext(), dialogTheme)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        bindingInflater(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.setupGUI()
        savedInstanceState?.let(::loadState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        run()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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

    protected fun bottomSheetDialog(block: BottomSheetDialog.() -> Unit) = (dialog as BottomSheetDialog).apply(block)

    protected fun window(block: Window.() -> Unit) = dialog?.window?.apply(block)

}