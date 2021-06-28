package com.kroegerama.kaiteki.example

import androidx.fragment.app.FragmentManager
import com.kroegerama.kaiteki.baseui.ViewBindingMaterialDialogFragment
import com.kroegerama.kaiteki.example.databinding.FragExampleBinding
import com.kroegerama.kaiteki.removeByTag

class VBMaterialDialog : ViewBindingMaterialDialogFragment<FragExampleBinding>(
    FragExampleBinding::inflate
) {

    override val overrideThemeResId = R.style.ThemeOverlay_KaitekiDialog

    companion object {
        private const val TAG = "vb_material_dialog"

        fun show(fm: FragmentManager) {
            fm.removeByTag(TAG)
            VBMaterialDialog().apply {

            }.show(fm, TAG)
        }
    }

}
