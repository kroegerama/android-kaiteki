package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class ViewBindingActivity<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater) -> VB,
    @MenuRes protected val optionsMenu: Int = 0
) : AppCompatActivity() {

    private val binding by lazy { bindingInflater(layoutInflater) }

    protected fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()

        setContentView(binding.root)
        binding.setupGUI()

        savedInstanceState?.let(::loadState)
        run()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu) =
        super.onCreateOptionsMenu(menu) || optionsMenu.let {
            if (it > 0) {
                menuInflater.inflate(it, menu)
                true
            } else false
        }

    protected open fun prepare() {}

    protected open fun VB.setupGUI() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected open fun run() {}

}