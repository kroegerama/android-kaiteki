package com.kroegerama.kaiteki

import android.content.Context
import androidx.annotation.RawRes
import java.nio.charset.Charset

fun Context.getRawResAsString(@RawRes rawRes: Int, charset: Charset = Charsets.UTF_8) =
    resources.openRawResource(rawRes).bufferedReader(charset).use { reader ->
        reader.readText()
    }