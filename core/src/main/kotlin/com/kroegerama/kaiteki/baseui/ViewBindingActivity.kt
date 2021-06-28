package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import androidx.annotation.CallSuper
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class ViewBindingActivity<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    @MenuRes
    protected open val optionsMenu: Int = 0

    protected val binding by lazy { bindingInflater(layoutInflater) }

    protected inline fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        prepare()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.setupGUI()

        savedInstanceState?.let(::loadState)
        run()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    @CallSuper
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val superResult = super.onCreateOptionsMenu(menu)
        val ownResult = optionsMenu.let {
            if (it > 0) {
                menuInflater.inflate(it, menu)
                true
            } else false
        }
        return superResult || ownResult
    }

    protected open fun prepare() {}

    protected open fun VB.setupGUI() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected open fun run() {}

}