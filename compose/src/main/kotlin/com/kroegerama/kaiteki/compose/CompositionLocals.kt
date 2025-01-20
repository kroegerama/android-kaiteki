package com.kroegerama.kaiteki.compose

import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

val LocalLocale = compositionLocalWithComputedDefaultOf<Locale> {
    LocalConfiguration.currentValue.locales[0]
}
