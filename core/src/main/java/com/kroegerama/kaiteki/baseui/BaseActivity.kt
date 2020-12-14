package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.Menu
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity(
    @LayoutRes protected val layout: Int,
    @MenuRes protected val optionsMenu: Int = 0
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepare()
        setContentView(layout)
        setupGUI()

        intent?.extras?.let(::handleArguments)
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

    protected open fun setupGUI() {}

    protected open fun handleArguments(args: Bundle) {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}

    protected open fun run() {}

}