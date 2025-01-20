package com.kroegerama.kaiteki.compose.formatting

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.util.Currency
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi
import com.kroegerama.kaiteki.compose.LocalLocale
import java.util.Locale

@ExperimentalKaitekiComposeApi
@Stable
class CurrencyOutputTransformation(
    private val grouping: Boolean = true,
    locale: Locale = Locale.getDefault(),
    currency: Currency = Currency.getInstance(locale)
) : OutputTransformation {

    private val decimalSeparator: Char
    private val groupingSeparator: Char
    private val prefix: String
    private val suffix: String

    init {
        val symbols = DecimalFormatSymbols.getInstance(locale)
        decimalSeparator = symbols.decimalSeparator
        groupingSeparator = symbols.groupingSeparator

        val df = DecimalFormat.getCurrencyInstance(locale) as DecimalFormat
        df.currency = currency
        prefix = df.positivePrefix
        suffix = df.positiveSuffix
    }

    override fun TextFieldBuffer.transformOutput() {
        if (grouping) {
            val intPart = originalText.split(decimalSeparator, limit = 2).firstOrNull() ?: ""
            for (index in 1 until intPart.length) {
                if (index % 3 == 0) {
                    insert(intPart.length - index, groupingSeparator.toString())
                }
            }
        }
        if (length > 0) {
            insert(0, prefix)
            append(suffix)
        }
    }
}

@ExperimentalKaitekiComposeApi
@Composable
fun rememberCurrencyOutputTransformation(
    grouping: Boolean = true,
    locale: Locale = LocalLocale.current,
    currency: Currency = remember(locale) { Currency.getInstance(locale) }
) = remember(currency, grouping, locale) {
    CurrencyOutputTransformation(
        currency = currency,
        grouping = grouping,
        locale = locale
    )
}
