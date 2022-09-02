package com.kroegerama.kaiteki.baseui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.annotation.CallSuper
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.R

abstract class ViewBindingFullscreenDialogFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : DialogFragment() {

    protected lateinit var binding: VB

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = ComponentDialog(requireContext(), themeResId)

    override fun getDialog(): ComponentDialog? {
        return super.getDialog() as? ComponentDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return bindingInflater(layoutInflater, container, false).also {
            binding = it
        }.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupGUI()

        dialog?.window?.let {
            if (!decorFitsSystemWindows) {
                it.statusBarColor = Color.TRANSPARENT
                it.navigationBarColor = Color.TRANSPARENT
            }
            WindowCompat.setDecorFitsSystemWindows(it, decorFitsSystemWindows)
        }
    }

    protected open val themeResId: Int = R.style.ThemeOverlay_Kaiteki_Material3_FullscreenDialog
    protected open fun VB.setupGUI() {}
    protected open val decorFitsSystemWindows = true
}
