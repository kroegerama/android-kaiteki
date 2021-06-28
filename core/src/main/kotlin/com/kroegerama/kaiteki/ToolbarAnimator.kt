package com.kroegerama.kaiteki

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar

fun Toolbar.injectTransition(duration: Long = 200) {
    val listener = object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewRemoved(parent: View, child: View) {}

        override fun onChildViewAdded(parent: View, child: View) {
            if (child is ActionMenuView) {
                child.layoutTransition = LayoutTransition().apply {
                    setDuration(duration)
                }
            }
        }
    }
    setOnHierarchyChangeListener(listener)
}