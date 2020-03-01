package com.kroegerama.kaiteki

import android.os.Bundle

fun bundle(block: Bundle.() -> Unit) = Bundle().apply(block)

fun String?.nullIfBlank() = if (isNullOrBlank()) null else this
