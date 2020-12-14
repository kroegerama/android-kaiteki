package com.kroegerama.kaiteki.architecture

import android.os.Bundle
import androidx.navigation.NavDestination

private val navLabelArgRegex by lazy { "\\{(.+?)\\}".toRegex() }

fun NavDestination.labelWithArgs(args: Bundle?): CharSequence? = with(label) {
    if (isNullOrBlank() || args == null || !contains('{') || !contains('}')) return this
    return replace(navLabelArgRegex) { r ->
        val key = r.groupValues[1]
        if (args.containsKey(key)) {
            args.get(key)?.toString() ?: r.value
        } else {
            r.value
        }
    }
}
