package com.kroegerama.kaiteki.compose.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.LayoutDirection

@Stable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    // use `LayoutDirection.Ltr` to get the "real" start/end padding values
    return PaddingValues(
        start = calculateStartPadding(LayoutDirection.Ltr) + other.calculateStartPadding(LayoutDirection.Ltr),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(LayoutDirection.Ltr),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}
