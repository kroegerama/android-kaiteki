package com.kroegerama.kaiteki.example

import com.kroegerama.kaiteki.baseui.ViewBindingActivity
import com.kroegerama.kaiteki.example.databinding.AcMainBinding

class VBActivity : ViewBindingActivity<AcMainBinding>(AcMainBinding::inflate) {

    override fun AcMainBinding.setupGUI() {
        btnTest.setOnClickListener { finish() }
    }

}