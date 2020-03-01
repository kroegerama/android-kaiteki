package com.kroegerama.kaiteki

import android.graphics.Paint

val Paint.FontMetrics.lineHeight get() = descent - ascent

val Paint.FontMetricsInt.lineHeight get() = descent - ascent