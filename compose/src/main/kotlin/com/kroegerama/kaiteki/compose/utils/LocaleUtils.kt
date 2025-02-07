package com.kroegerama.kaiteki.compose.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf
import java.util.Locale

val Locale.unicodeFlag: String
    @Stable
    get() {
        val countryCode = country.uppercase(Locale.ROOT)
        return StringBuilder(countryCode.length * 2).apply {
            for (char in countryCode) {
                appendCodePoint(char.code + 0x1F1A5)
            }
        }.toString()
    }

@Preview(showBackground = true)
@Composable
private fun UnicodeFlagPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        persistentListOf(
            Locale.GERMANY,
            Locale("", "IE"),
            Locale.UK,
            Locale.FRANCE,
            Locale.JAPAN,
            Locale.ITALY,
            Locale.US,
            Locale.CANADA,
            Locale.KOREA,
            Locale.TAIWAN,
            Locale.CHINA,
            Locale("", "ES"),
            Locale("", "LK")
        ).forEach {
            Text(
                text = it.unicodeFlag + "\u2003" + it.displayCountry,
                fontSize = 24.sp
            )
        }
    }
}
