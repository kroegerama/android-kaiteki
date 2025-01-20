package com.kroegerama.kaiteki.compose.formatting

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kroegerama.kaiteki.compose.ExperimentalKaitekiComposeApi
import java.util.Locale

@Preview(showBackground = true)
@ExperimentalKaitekiComposeApi
@Composable
private fun CurrencyTransformationExample() {
    val locale = Locale.GERMANY

    val decimalFormat = rememberDecimalFormat(locale = locale)
    val inputTransformation = rememberDecimalInputTransformation(
        maxDecimalPlaces = 2,
        locale = locale
    )
    val outputTransformation = rememberCurrencyOutputTransformation(
        locale = locale,
//        currency = Currency.getInstance("EUR")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Card {
            val state = rememberTextFieldState(
                initialText = decimalFormat.format(12345.23)
            )
            BasicTextField(
                state = state,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
        val dfs = DecimalFormatSymbols.getInstance(locale)
        Text(
            text = dfs.currencySymbol
        )

        val df = DecimalFormat.getCurrencyInstance(Locale.GERMANY) as DecimalFormat
//        df.currency = Currency.getInstance("EUR")

        Text(
            text = "negativePrefix> '" + (df.negativePrefix ?: "?") + "'"
        )
        Text(
            text = "positivePrefix> '" + (df.positivePrefix ?: "?") + "'"
        )
        Text(
            text = "negativeSuffix> '" + (df.negativeSuffix ?: "?") + "'"
        )
        Text(
            text = "positiveSuffix> '" + (df.positiveSuffix ?: "?") + "'"
        )
    }
}
