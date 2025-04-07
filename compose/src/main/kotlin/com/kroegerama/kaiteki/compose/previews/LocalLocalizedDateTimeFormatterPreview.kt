package com.kroegerama.kaiteki.compose.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kroegerama.kaiteki.compose.LocalLocalizedDateTimeFormatter
import com.kroegerama.kaiteki.compose.formatting.rememberLocalizedDateTimeFormatter
import java.time.OffsetDateTime
import java.time.format.FormatStyle

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
