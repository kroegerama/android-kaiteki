package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.kroegerama.kaiteki.MenuProviderOwner

abstract class ViewBindingActivity<VB : ViewBinding>(
    protected val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected val binding by lazy { bindingInflater(layoutInflater) }

    protected inline fun binding(block: VB.() -> Unit) {
        binding.apply(block)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        prepare()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        when (this) {
            is MenuProviderOwner -> addMenuProvider(menuProvider, this, Lifecycle.State.STARTED)
            is MenuProvider -> addMenuProvider(this, this, Lifecycle.State.STARTED)
        }
        binding.setupGUI()

        savedInstanceState?.let(::loadState)
        run()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    protected open fun prepare() {}

    protected open fun VB.setupGUI() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected open fun run() {}

}