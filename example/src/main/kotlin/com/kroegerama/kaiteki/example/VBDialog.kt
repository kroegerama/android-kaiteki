package com.kroegerama.kaiteki.example

import com.kroegerama.kaiteki.baseui.ViewBindingBottomSheetDialogFragment
import com.kroegerama.kaiteki.baseui.ViewBindingMaterialDialogFragment
import com.kroegerama.kaiteki.example.databinding.FragExampleBinding

class VBDialog : ViewBindingMaterialDialogFragment<FragExampleBinding>(FragExampleBinding::inflate) {


}

class VBBottom : ViewBindingBottomSheetDialogFragment<FragExampleBinding>(FragExampleBinding::inflate) {

}