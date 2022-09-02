package com.kroegerama.kaiteki

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import com.google.android.material.card.MaterialCardView

fun View.doOnApplyWindowInsets(
    block: WindowInsetsCompat.(v: View, originalPadding: Insets) -> Unit
) {
    val originalPadding = Insets.of(
        paddingLeft, paddingTop, paddingRight, paddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        insets.block(v, originalPadding)
        insets
    }
    doOnAttach {
        requestApplyInsets()
    }
}

fun View.addWindowInsetsPadding(
    top: Boolean = false,
    bottom: Boolean = false,
    block: ((v: View, originalPadding: Insets) -> Unit)? = null
) = doOnApplyWindowInsets { v, originalPadding ->
    val systemBars = getInsets(WindowInsetsCompat.Type.systemBars())
    v.updatePadding(
        top = originalPadding.top + (systemBars.top.takeIf { top } ?: 0),
        bottom = originalPadding.bottom + (systemBars.bottom.takeIf { bottom } ?: 0)
    )
    block?.invoke(v, originalPadding)
}

fun View.handleSystemBarsInsets(top: Boolean = false, bottom: Boolean = false) = doOnApplyWindowInsets { v, originalPadding ->
    val sysBars = getInsets(WindowInsetsCompat.Type.systemBars())
    val imeVisible = isVisible(WindowInsetsCompat.Type.ime())
    updatePadding(
        top = if (top) sysBars.top + originalPadding.top else originalPadding.top,
        bottom = if (bottom && !imeVisible) sysBars.bottom + originalPadding.bottom else originalPadding.bottom,
    )
}

fun MaterialCardView.handleSystemBarsInsets(top: Boolean = false, bottom: Boolean = false) {
    val originalPadding = Insets.of(
        contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

        setContentPadding(
            originalPadding.left,
            if (top) sysBars.top + originalPadding.top else originalPadding.top,
            originalPadding.right,
            if (bottom && !imeVisible) sysBars.bottom + originalPadding.bottom else originalPadding.bottom,
        )

        insets
    }
    doOnAttach {
        requestApplyInsets()
    }
}
