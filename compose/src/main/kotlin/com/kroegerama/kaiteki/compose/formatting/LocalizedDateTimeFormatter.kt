package com.kroegerama.kaiteki.compose.formatting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kroegerama.kaiteki.compose.LocalLocale
import com.kroegerama.kaiteki.compose.LocalLocalizedDateTimeFormatter
import com.kroegerama.kaiteki.compose.LocalZoneId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.util.Locale

@Immutable
interface LocalizedDateTimeFormatter {
    @Stable
    fun formatDate(temporal: Temporal): String

    @Stable
    fun formatTime(temporal: Temporal): String

    @Stable
    fun formatDateTime(temporal: Temporal): String
}

internal class DefaultLocalizedDateTimeFormatter(
    dateStyle: FormatStyle,
    timeStyle: FormatStyle,
    locale: Locale,
    zoneId: ZoneId
) : LocalizedDateTimeFormatter {
    private val dateFormatter = DateTimeFormatter
        .ofLocalizedDate(dateStyle)
        .withLocale(locale)
        .withZone(zoneId)

    private val timeFormatter = DateTimeFormatter
        .ofLocalizedTime(timeStyle)
        .withLocale(locale)
        .withZone(zoneId)

    private val dateTimeFormatter = DateTimeFormatter
        .ofLocalizedDateTime(dateStyle, timeStyle)
        .withLocale(locale)
        .withZone(zoneId)

    override fun formatDate(temporal: Temporal): String = dateFormatter.format(temporal)
    override fun formatTime(temporal: Temporal): String = timeFormatter.format(temporal)
    override fun formatDateTime(temporal: Temporal): String = dateTimeFormatter.format(temporal)
}

@Composable
fun rememberLocalizedDateTimeFormatter(
    dateStyle: FormatStyle = FormatStyle.MEDIUM,
    timeStyle: FormatStyle = FormatStyle.SHORT,
    locale: Locale = LocalLocale.current,
    zoneId: ZoneId = LocalZoneId.current
): LocalizedDateTimeFormatter = remember(dateStyle, timeStyle, locale, zoneId) {
    DefaultLocalizedDateTimeFormatter(
        dateStyle = dateStyle,
        timeStyle = timeStyle,
        locale = locale,
        zoneId = zoneId
    )
}

@Preview(showBackground = true)
@Composable
private fun LocalLocalizedDateTimeFormatterPreview() {
    val dateTime = remember {
        OffsetDateTime.now()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "not set"
        )
        Text(
            text = LocalLocalizedDateTimeFormatter.current.formatDateTime(dateTime)
        )
        val localFormatter = rememberLocalizedDateTimeFormatter(
            dateStyle = FormatStyle.LONG,
            timeStyle = FormatStyle.MEDIUM
        )
        HorizontalDivider()
        CompositionLocalProvider(
            LocalLocalizedDateTimeFormatter provides localFormatter
        ) {
            Text(
                text = "set"
            )
            Text(
                text = LocalLocalizedDateTimeFormatter.current.formatDateTime(dateTime)
            )
        }
    }
}
