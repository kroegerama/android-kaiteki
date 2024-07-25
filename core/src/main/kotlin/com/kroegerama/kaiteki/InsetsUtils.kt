package com.kroegerama.kaiteki

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
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

val NoopInsetsConsumer: WindowInsetsCompat.(insets: Insets) -> WindowInsetsCompat = { this }

val MarginReader: View.() -> RelativePadding = { RelativePadding(marginStart, marginTop, marginEnd, marginBottom) }
val PaddingReader: View.() -> RelativePadding = ::RelativePadding

val MarginUpdater: View.(start: Int, top: Int, end: Int, bottom: Int) -> Unit = { start, top, end, bottom ->
    updateLayoutParams<MarginLayoutParams> {
        marginStart = start
        topMargin = top
        marginEnd = end
        bottomMargin = bottom
    }
}
val PaddingUpdater: View.(start: Int, top: Int, end: Int, bottom: Int) -> Unit = View::setPaddingRelative

val DefaultTypeMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()

fun <T : View> T.doOnApplyWindowInsetsRelative(
    paddingReader: T.() -> RelativePadding = PaddingReader,
    block: WindowInsetsCompat.(v: View, originalPadding: RelativePadding) -> WindowInsetsCompat
) {
    val originalPadding = paddingReader()
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        insets.block(v, originalPadding)
    }
    doOnAttach {
        requestApplyInsets()
    }
}

fun <T : View> T.handleWindowInsets(
    edge: WindowInsetsEdge,
    typeMask: Int = DefaultTypeMask,
    insetsConsumer: WindowInsetsCompat.(insets: Insets) -> WindowInsetsCompat = NoopInsetsConsumer,
    paddingReader: T.() -> RelativePadding = PaddingReader,
    paddingUpdater: T.(start: Int, top: Int, end: Int, bottom: Int) -> Unit = PaddingUpdater
) {
    if (this is ViewGroup && clipToPadding) {
        logger.warning("applying window insets to $javaClass, but clipToPadding is true")
    }
    fun Int.takeIf(take: Boolean) = if (take) this else 0
    val isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL

    doOnApplyWindowInsetsRelative(paddingReader = paddingReader) { _, originalPadding ->
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

        insetsConsumer(bars)
    }
}

fun MaterialCardView.handleWindowInsets(
    edge: WindowInsetsEdge,
    typeMask: Int = DefaultTypeMask,
    insetsConsumer: WindowInsetsCompat.(insets: Insets) -> WindowInsetsCompat = NoopInsetsConsumer
) = handleWindowInsets(
    edge = edge,
    typeMask = typeMask,
    insetsConsumer = insetsConsumer,
    paddingReader = {
        val isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
        val contentPaddingStart = if (isRtl) contentPaddingRight else contentPaddingLeft
        val contentPaddingEnd = if (isRtl) contentPaddingLeft else contentPaddingRight
        RelativePadding(contentPaddingStart, contentPaddingTop, contentPaddingEnd, contentPaddingBottom)
    },
    paddingUpdater = { start, top, end, bottom ->
        val isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
        setContentPadding(
            if (isRtl) end else start,
            top,
            if (isRtl) start else end,
            bottom
        )
    }
)

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
    fun insetsConsumer(view: View): WindowInsetsCompat.(insets: Insets) -> WindowInsetsCompat = { insets ->
        val isRtl = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL
        inset(this, insets, isRtl)
    }

    fun inset(original: WindowInsetsCompat, insets: Insets, isRtl: Boolean): WindowInsetsCompat {
        val left = if (isRtl) end else start
        val right = if (isRtl) start else end
        return original.inset(
            if (left) insets.left else 0,
            if (top) insets.top else 0,
            if (right) insets.right else 0,
            if (bottom) insets.bottom else 0
        )
    }

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
