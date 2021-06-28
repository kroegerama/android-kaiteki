package com.kroegerama.kaiteki.baseui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class ViewBindingMaterialDialogFragment<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    @StyleRes
    protected open val overrideThemeResId: Int = 0

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext(), overrideThemeResId).apply {
            val view = bindingInflater(layoutInflater, null, false).also { _binding = it }.root
            setView(view)
            setupDialog()
        }.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = binding.root

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    protected open fun MaterialAlertDialogBuilder.setupDialog() {}

    protected open fun VB.setupGUI() {}

    protected open fun run() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

}
