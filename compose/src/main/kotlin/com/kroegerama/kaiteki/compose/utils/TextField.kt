package com.kroegerama.kaiteki.compose.utils

import androidx.compose.foundation.text.input.TextFieldState

val TextFieldState.string: String get() = text.toString()
