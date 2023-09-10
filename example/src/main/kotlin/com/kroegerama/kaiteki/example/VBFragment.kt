package com.kroegerama.kaiteki.example

import com.kroegerama.kaiteki.baseui.ViewBindingFragment
import com.kroegerama.kaiteki.callFirstListener
import com.kroegerama.kaiteki.example.databinding.FragExampleBinding

@Suppress("DEPRECATION")
class VBFragment : ViewBindingFragment<FragExampleBinding>(
    FragExampleBinding::inflate
) {

    override fun FragExampleBinding.setupGUI() {
        btnTest.setOnClickListener { requireActivity().finish() }
        val callSuccess = callFirstListener<FragListener> { onNotify() }
    }

    interface FragListener {
        fun onNotify()
    }

    companion object {
        fun makeInstance(index: Int) = VBFragment().apply {
        }
    }
}