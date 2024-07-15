package com.kroegerama.kaiteki

import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import com.google.android.material.card.MaterialCardView
import java.util.logging.Logger

private val logger = Logger.getLogger("InsetUtils")

@Deprecated("use doOnApplyWindowInsetsRelative")
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

@Deprecated("use handleWindowInsets")
@Suppress("DEPRECATION")
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

@Deprecated("use handleWindowInsets(edge)")
@Suppress("DEPRECATION")
fun View.handleSystemBarsInsets(top: Boolean = false, bottom: Boolean = false) = doOnApplyWindowInsets { _, originalPadding ->
    val sysBars = getInsets(WindowInsetsCompat.Type.systemBars())
    val imeVisible = isVisible(WindowInsetsCompat.Type.ime())
    updatePadding(
        top = if (top) sysBars.top + originalPadding.top else originalPadding.top,
        bottom = if (bottom && !imeVisible) sysBars.bottom + originalPadding.bottom else originalPadding.bottom,
    )
}

@Deprecated("use handleWindowInsets(edge)")
fun MaterialCardView.handleSystemBarsInsets(top: Boolean = false, bottom: Boolean = false) {
    val originalPadding = Insets.of(
        contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
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

fun View.doOnApplyWindowInsetsRelative(
    block: WindowInsetsCompat.(v: View, originalPadding: RelativePadding) -> WindowInsetsCompat
) {
    val originalPadding = RelativePadding(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        insets.block(v, originalPadding)
    }
    doOnAttach {
        requestApplyInsets()
    }
}

fun <T : View> T.handleWindowInsets(
    edge: WindowInsetsEdge,
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    consumer: (WindowInsetsCompat) -> WindowInsetsCompat = { WindowInsetsCompat.CONSUMED },
    paddingUpdater: T.(start: Int, top: Int, end: Int, bottom: Int) -> Unit = View::setPaddingRelative
) {
    if (this is ViewGroup && clipToPadding) {
        logger.warning("applying window insets to $javaClass, but clipToPadding is true")
    }
    fun Int.takeIf(take: Boolean) = if (take) this else 0
    val isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL

    doOnApplyWindowInsetsRelative { _, originalPadding ->
        val bars = getInsets(typeMask)
        val barsStart = if (isRtl) bars.right else bars.left
        val barsEnd = if (isRtl) bars.left else bars.right
        logger.info("merged window insets: $bars")

        paddingUpdater(
            barsStart.takeIf(edge.start) + originalPadding.start,
            bars.top.takeIf(edge.top) + originalPadding.top,
            barsEnd.takeIf(edge.end) + originalPadding.end,
            bars.bottom.takeIf(edge.bottom) + originalPadding.bottom
        )

        consumer(this)
    }
}

fun MaterialCardView.handleWindowInsets(
    edge: WindowInsetsEdge,
    typeMask: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
    consumer: (WindowInsetsCompat) -> WindowInsetsCompat = { WindowInsetsCompat.CONSUMED },
) = handleWindowInsets(edge, typeMask, consumer) { start, top, end, bottom ->
    val isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
    setContentPadding(
        if (isRtl) end else start,
        top,
        if (isRtl) start else end,
        bottom
    )
}

data class RelativePadding(
    val start: Int,
    val top: Int,
    val end: Int,
    val bottom: Int
) {
    constructor(view: View) : this(view.paddingStart, view.paddingTop, view.paddingEnd, view.paddingBottom)
}

data class WindowInsetsEdge(
    val start: Boolean = false,
    val top: Boolean = false,
    val end: Boolean = false,
    val bottom: Boolean = false
) {
    companion object {
        val Start = WindowInsetsEdge(
            start = true,
            top = true,
            bottom = true
        )

        val StartOnly = WindowInsetsEdge(
            start = true
        )

        val Top = WindowInsetsEdge(
            top = true,
            start = true,
            end = true
        )

        val TopOnly = WindowInsetsEdge(
            top = true
        )

        val End = WindowInsetsEdge(
            end = true,
            top = true,
            bottom = true
        )

        val EndOnly = WindowInsetsEdge(
            end = true
        )

        val Bottom = WindowInsetsEdge(
            bottom = true,
            start = true,
            end = true
        )

        val BottomOnly = WindowInsetsEdge(
            bottom = true
        )

        val Horizontal = WindowInsetsEdge(
            start = true,
            end = true
        )

        val Vertical = WindowInsetsEdge(
            top = true,
            bottom = true
        )

        val All = WindowInsetsEdge(
            start = true,
            top = true,
            end = true,
            bottom = true
        )
    }
}
