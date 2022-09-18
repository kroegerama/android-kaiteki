package com.kroegerama.kaiteki

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

internal class ViewAttachedScope : CoroutineScope, View.OnAttachStateChangeListener {
    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    override fun onViewAttachedToWindow(view: View) = Unit

    override fun onViewDetachedFromWindow(view: View) {
        view.removeOnAttachStateChangeListener(this)
        view.setTag(R.id.view_attached_scope, null)
        coroutineContext.cancel()
    }

}

val View.attachedToWindowScope: CoroutineScope
    get() {
        val existing = getTag(R.id.view_attached_scope) as? CoroutineScope
        if (existing != null) return existing

        val scope = ViewAttachedScope()
        setTag(R.id.view_attached_scope, scope)
        addOnAttachStateChangeListener(scope)

        return scope
    }
