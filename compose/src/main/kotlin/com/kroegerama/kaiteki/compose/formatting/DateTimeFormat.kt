package com.kroegerama.kaiteki.compose.formatting

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kroegerama.kaiteki.compose.LocalLocale
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun rememberLocalizedDateTimeFormatter(
    dateStyle: FormatStyle,
    timeStyle: FormatStyle,
    locale: Locale = LocalLocale.current
): DateTimeFormatter = remember(
    dateStyle,
    timeStyle,
    locale
) {
    DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            localizedBy(locale)
        } else {
            withLocale(locale)
        }
    }.withZone(ZoneId.systemDefault())
}

@Composable
fun rememberLocalizedDateFormatter(
    dateStyle: FormatStyle,
    locale: Locale = LocalLocale.current
): DateTimeFormatter = remember(
    dateStyle,
    locale
) {
    DateTimeFormatter.ofLocalizedDate(dateStyle).run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            localizedBy(locale)
        } else {
            withLocale(locale)
        }
    }.withZone(ZoneId.systemDefault())
}

@Composable
fun rememberLocalizedTimeFormatter(
    timeStyle: FormatStyle,
    locale: Locale = LocalLocale.current
): DateTimeFormatter = remember(
    timeStyle,
    locale
) {
    DateTimeFormatter.ofLocalizedDateTime(timeStyle).run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            localizedBy(locale)
        } else {
            withLocale(locale)
        }
    }.withZone(ZoneId.systemDefault())
}
