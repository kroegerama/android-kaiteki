package com.kroegerama.kaiteki.compose.formatting

import android.icu.text.DecimalFormatSymbols
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi
import com.kroegerama.kaiteki.compose.LocalLocale
import java.util.Locale
import kotlin.math.min

@ExperimentalKaitekiComposeApi
@Stable
class DecimalInputTransformation(
    private val maxDecimalPlaces: Int,
    locale: Locale = Locale.getDefault()
) : InputTransformation {

    private val decimalSeparator: Char
    private val decimalSeparatorConsecutive: String

    init {
        val symbols = DecimalFormatSymbols.getInstance(locale)
        decimalSeparator = symbols.decimalSeparator
        decimalSeparatorConsecutive = "$decimalSeparator$decimalSeparator"
    }

    override val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Decimal,
    )

    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().any { !it.isDigit() && it != decimalSeparator }) {
            revertAllChanges()
        }
        if (decimalSeparatorConsecutive in asCharSequence()) {
            revertAllChanges()
        }

        val parts = asCharSequence().split(decimalSeparator)
        val intPart = parts.firstOrNull()?.filter { it.isDigit() } ?: ""
        val decimalPart = parts.getOrNull(1)?.filter { it.isDigit() }

        if (parts.size > 2) {
            replace(intPart.length + 1, length, decimalPart ?: "")
        }

        val filteredIntPart = filterIntPart(intPart, hasDecimalPart = decimalPart != null)

        filterDecimalPart(decimalPart, startIndex = filteredIntPart.length + 1)
    }

    private fun TextFieldBuffer.filterIntPart(
        intPart: String,
        hasDecimalPart: Boolean,
    ): String {
        var mutableIntPart = intPart

        if (mutableIntPart.startsWith('0')) {
            // Allow single leading zero. Replace leading zero if followed by another digit.
            val indexOfFirstNonZero = mutableIntPart.indexOfFirst { it != '0' }
            val newIntPart = if (indexOfFirstNonZero > 0) {
                mutableIntPart.substring(indexOfFirstNonZero)
            } else {
                "0"
            }
            replace(0, mutableIntPart.length, newIntPart)
            mutableIntPart = newIntPart
        }

        if (mutableIntPart.isEmpty() && hasDecimalPart) {
            // Prefill 0 when only decimal part is entered
            val newIntPart = "0"
            replace(0, mutableIntPart.length, newIntPart)
            mutableIntPart = newIntPart
        }

        return mutableIntPart
    }

    private fun TextFieldBuffer.filterDecimalPart(
        decimalPart: String?,
        startIndex: Int,
    ) {
        if (decimalPart != null && decimalPart.length > maxDecimalPlaces) {
            // Replace chars one-by-one so the cursor advances as expected
            for (index in startIndex until min(maxDecimalPlaces, decimalPart.length)) {
                replace(index, index + 1, decimalPart[index].toString())
            }
            delete(startIndex + maxDecimalPlaces, length)
        }
    }
}

@ExperimentalKaitekiComposeApi
@Composable
fun rememberDecimalInputTransformation(
    maxDecimalPlaces: Int,
    locale: Locale = LocalLocale.current
) = remember(maxDecimalPlaces, locale) {
    DecimalInputTransformation(
        maxDecimalPlaces = maxDecimalPlaces,
        locale = locale
    )
}
