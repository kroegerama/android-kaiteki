package com.kroegerama.kaiteki.example

import com.kroegerama.kaiteki.baseui.ViewBindingFragment
import com.kroegerama.kaiteki.example.databinding.FragExampleBinding

class VBFragment : ViewBindingFragment<FragExampleBinding>(
    FragExampleBinding::inflate
) {

    override fun FragExampleBinding.setupGUI() {
        btnTest.setOnClickListener { activity!!.finish() }
    }

    companion object {
        fun makeInstance(index: Int) = VBFragment().apply {
        }
    }
}