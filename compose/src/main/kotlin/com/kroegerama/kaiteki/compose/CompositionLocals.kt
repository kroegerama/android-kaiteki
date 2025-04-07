package com.kroegerama.kaiteki.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration
import com.kroegerama.kaiteki.compose.formatting.DefaultLocalizedDateTimeFormatter
import com.kroegerama.kaiteki.compose.formatting.LocalizedDateTimeFormatter
import java.time.ZoneId
import java.time.format.FormatStyle
import java.util.Locale

val LocalLocale = compositionLocalWithComputedDefaultOf<Locale> {
    LocalConfiguration.currentValue.locales[0] ?: Locale.getDefault()
}

val LocalZoneId = compositionLocalOf<ZoneId> {
    ZoneId.systemDefault()
}

val LocalLocalizedDateTimeFormatter = compositionLocalOf<LocalizedDateTimeFormatter> {
    DefaultLocalizedDateTimeFormatter(
        dateStyle = FormatStyle.MEDIUM,
        timeStyle = FormatStyle.SHORT,
        locale = Locale.getDefault(),
        zoneId = ZoneId.systemDefault()
    )
}
