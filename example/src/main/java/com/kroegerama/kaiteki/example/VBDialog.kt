package com.kroegerama.kaiteki.example

import com.kroegerama.kaiteki.baseui.ViewBindingBottomSheetDialogFragment
import com.kroegerama.kaiteki.baseui.ViewBindingDialogFragment
import com.kroegerama.kaiteki.example.databinding.FragExampleBinding

class VBDialog : ViewBindingDialogFragment<FragExampleBinding>(FragExampleBinding::inflate) {


}

class VBBottom : ViewBindingBottomSheetDialogFragment<FragExampleBinding>(FragExampleBinding::inflate) {

}