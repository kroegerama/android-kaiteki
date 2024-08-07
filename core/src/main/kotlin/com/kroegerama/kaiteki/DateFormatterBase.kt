package com.kroegerama.kaiteki

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.Temporal
import kotlin.math.absoluteValue

abstract class DateFormatterBase(
    @StringRes protected val yesterdayStringRes: Int,
    @StringRes protected val todayStringRes: Int,
    @StringRes protected val tomorrowStringRes: Int,
    @PluralsRes protected val daysAgoRes: Int,
    @PluralsRes protected val inDaysRes: Int,
    @PluralsRes protected val hoursAgoRes: Int,
    @PluralsRes protected val inHoursRes: Int,
    @PluralsRes protected val minutesAgoRes: Int,
    @PluralsRes protected val inMinutesRes: Int,
    @PluralsRes protected val secondsAgoRes: Int,
    @PluralsRes protected val inSecondsRes: Int,
    private val defaultDateTimeDelimiter: String = " • ",
    protected val contextProvider: () -> Context
) {

    open val dateFormat: DateTimeFormatter by lazy { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    open val timeFormat: DateTimeFormatter by lazy { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    open val monthYearFormat: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("MMMM yyyy") }
    open val yearFormat: DateTimeFormatter by lazy { DateTimeFormatter.ofPattern("yyyy") }

    fun Temporal.localized(): Temporal = when (this) {
        is OffsetDateTime -> withOffsetSameInstant(OffsetDateTime.now().offset)
        is ZonedDateTime -> withZoneSameInstant(ZoneId.systemDefault())
        is LocalDate -> atStartOfDay()
        else -> this
    }

    fun Temporal.formatDateTime(dateTimeDelimiter: String = defaultDateTimeDelimiter): String = buildString {
        append(formatDate())
        append(dateTimeDelimiter)
        append(formatTime())
    }

    fun Temporal.formatDate(): String = localized().format(dateFormat)
    fun Temporal.formatTime(): String = localized().format(timeFormat)
    fun Temporal.formatMonthYear(): String = localized().format(monthYearFormat)
    fun Temporal.formatYear(): String = localized().format(yearFormat)

    fun Temporal.formatFancy(
        useDuration: Boolean = false,
        durationSwitchOver: Duration = defaultDurationSwitchOver,
        daysSwitchOver: Duration = defaultDaysSwitchOver,
        hoursSwitchOver: Duration = defaultHoursSwitchOver,
        minutesSwitchOver: Duration = defaultMinutesSwitchOver,
        dateTimeDelimiter: String = defaultDateTimeDelimiter
    ): String = buildString {
        if (useDuration) {
            val age = Duration.between(OffsetDateTime.now(), this@formatFancy)
            if (age.abs() < durationSwitchOver) {
                append(
                    age.formatFancy(
                        daysSwitchOver = daysSwitchOver,
                        hoursSwitchOver = hoursSwitchOver,
                        minutesSwitchOver = minutesSwitchOver
                    )
                )
                return@buildString
            }
        }

        val offset = getDayOffset()
        when (offset) {
            -1L -> append(getString(yesterdayStringRes))
            0L -> append(getString(todayStringRes))
            1L -> append(getString(tomorrowStringRes))
            else -> append(formatDate())
        }
        append(dateTimeDelimiter)
        append(formatTime())
    }

    fun Duration.formatFancy(
        daysSwitchOver: Duration = defaultDaysSwitchOver,
        hoursSwitchOver: Duration = defaultHoursSwitchOver,
        minutesSwitchOver: Duration = defaultMinutesSwitchOver
    ): String {
        val abs = abs()

        if (abs >= daysSwitchOver) {
            val days = toDays()
            return if (days < 0) {
                getPlural(daysAgoRes, days.absoluteValue)
            } else {
                getPlural(inDaysRes, days.absoluteValue)
            }
        }
        if (abs >= hoursSwitchOver) {
            val hours = toHours()
            return if (hours < 0) {
                getPlural(hoursAgoRes, hours.absoluteValue)
            } else {
                getPlural(inHoursRes, hours.absoluteValue)
            }
        }
        if (abs >= minutesSwitchOver) {
            val minutes = toMinutes()
            return if (minutes < 0) {
                getPlural(minutesAgoRes, minutes.absoluteValue)
            } else {
                getPlural(inMinutesRes, minutes.absoluteValue)
            }
        }
        return if (seconds <= 0) {
            val seconds = toSeconds()
            getPlural(secondsAgoRes, seconds.absoluteValue)
        } else {
            getPlural(inSecondsRes, seconds.absoluteValue)
        }
    }

    protected fun getPlural(@PluralsRes res: Int, value: Number): String = contextProvider().resources.getQuantityString(res, value.toInt(), value)
    protected fun getString(@StringRes res: Int): String = contextProvider().getString(res)

    fun Temporal.getDayOffset(): Long = localized().run {
        val epochDay = getLong(ChronoField.EPOCH_DAY)
        val nowEpochDay = LocalDate.now().getLong(ChronoField.EPOCH_DAY)
        return epochDay - nowEpochDay
    }

    protected fun Temporal.format(formatter: DateTimeFormatter): String = formatter.format(this)

    fun ClosedRange<LocalDate>.format(): String = buildString {
        append(start.formatDate())
        append(" \u2012 ")
        append(endInclusive.formatDate())
    }

    companion object {
        val defaultDurationSwitchOver: Duration = Duration.ofHours(12)
        val defaultDaysSwitchOver: Duration = Duration.ofDays(1)
        val defaultHoursSwitchOver: Duration = Duration.ofHours(2)
        val defaultMinutesSwitchOver: Duration = Duration.ofMinutes(2)
    }

}
