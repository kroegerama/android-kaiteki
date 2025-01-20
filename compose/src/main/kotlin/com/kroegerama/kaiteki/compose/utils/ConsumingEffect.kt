package com.kroegerama.kaiteki.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import arrow.core.Option

@Composable
@NonRestartableComposable
fun <T> AutoConsumeOptionEffect(option: Option<T>, onConsumed: (T) -> Unit, action: suspend (T) -> Unit) {
    LaunchedEffect(key1 = option) {
        option.onSome {
            action(it)
            onConsumed(it)
        }
    }
}
