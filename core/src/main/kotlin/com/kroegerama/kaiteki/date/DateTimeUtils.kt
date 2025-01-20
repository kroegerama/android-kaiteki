package com.kroegerama.kaiteki.date

import androidx.compose.runtime.Stable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.JulianFields
import java.time.temporal.Temporal
import java.time.temporal.TemporalQueries

@Stable
fun Temporal.julianDay(): Long {
    val zone = query(TemporalQueries.zone()) ?: ZoneId.systemDefault()
    return if (isSupported(ChronoField.INSTANT_SECONDS)) {
        val epochSecond = getLong(ChronoField.INSTANT_SECONDS)
        val nanoOfSecond = getLong(ChronoField.NANO_OF_SECOND)
        ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, nanoOfSecond), zone)
    } else {
        val date = query(TemporalQueries.localDate()) ?: LocalDate.now()
        val time = query(TemporalQueries.localTime()) ?: LocalTime.MIDNIGHT
        ZonedDateTime.of(date, time, zone)
    }.withZoneSameInstant(ZoneId.systemDefault()).getLong(JulianFields.JULIAN_DAY)
}

/**
 * example:
 *   - `-1` for yesterday
 *   - `0` for today
 *   - `1` for tomorrow
 */
@Stable
fun julianDayDistance(temporal: Temporal): Long = temporal.julianDay() - ZonedDateTime.now().julianDay()

@Stable
fun Temporal.epochMillis(): Long {
    return if (isSupported(ChronoField.INSTANT_SECONDS)) {
        val epochSecond = getLong(ChronoField.INSTANT_SECONDS)
        val nanoOfSecond = getLong(ChronoField.NANO_OF_SECOND)
        Instant.ofEpochSecond(epochSecond, nanoOfSecond).toEpochMilli()
    } else {
        val zone: ZoneId = query(TemporalQueries.zone()) ?: ZoneId.systemDefault()
        val date = query(TemporalQueries.localDate()) ?: LocalDate.now()
        val time = query(TemporalQueries.localTime()) ?: LocalTime.MIDNIGHT
        ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
    }
}

