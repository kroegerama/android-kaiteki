package com.kroegerama.kaiteki.compose.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toPersistentList
import java.text.Collator
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
    val items = remember {
        val collator = Collator.getInstance()
        Locale.getISOCountries().map {
            Locale("", it)
        }.sortedWith(
            Comparator.comparing(
                { it.displayCountry },
                collator
            )
        ).toPersistentList()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = items,
            key = { it }
        ) {
            Text(
                text = it.unicodeFlag + "\u2003" + it.displayCountry,
                fontSize = 18.sp,
                modifier = Modifier.heightIn(min = 32.dp)
            )
        }
    }
}
