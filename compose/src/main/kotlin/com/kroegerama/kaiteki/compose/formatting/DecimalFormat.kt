package com.kroegerama.kaiteki.compose.formatting

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kroegerama.kaiteki.compose.LocalLocale
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun rememberDecimalFormat(
    locale: Locale = LocalLocale.current,
    isGroupingUsed: Boolean = false
): NumberFormat = remember(locale, isGroupingUsed) {
    DecimalFormat.getInstance(locale).apply {
        this.isGroupingUsed = isGroupingUsed
    }
}
