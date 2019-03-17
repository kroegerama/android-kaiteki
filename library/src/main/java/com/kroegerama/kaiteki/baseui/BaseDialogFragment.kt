package com.kroegerama.kaiteki.baseui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(layoutResource, container, false).also {
                prepare()
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { loadState(it) }
        setupGUI()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    override fun onStart() {
        super.onStart()
        run()
    }

    @get:LayoutRes
    protected abstract val layoutResource: Int

    protected open fun prepare() {}

    protected open fun setupGUI() {}

    protected open fun run() {}

    protected open fun loadState(state: Bundle) {}

    protected open fun saveState(outState: Bundle) {}
}