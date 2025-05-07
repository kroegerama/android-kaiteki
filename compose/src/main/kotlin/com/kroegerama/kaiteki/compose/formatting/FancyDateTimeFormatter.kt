package com.kroegerama.kaiteki.compose.formatting

import android.icu.text.DisplayContext
import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit
import android.icu.util.ULocale
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.kroegerama.kaiteki.compose.LocalLocale
import com.kroegerama.kaiteki.date.epochMillis
import com.kroegerama.kaiteki.date.julianDayDistance
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal
import java.util.Locale
import kotlin.math.absoluteValue

@RequiresApi(Build.VERSION_CODES.N)
@Immutable
class FancyDateTimeFormatter(
    private val locale: Locale,
    private val skeletons: FancyDateTimeFormatterSkeletons,
    private val dateTimeDivider: String = ", ",
    style: RelativeDateTimeFormatter.Style = RelativeDateTimeFormatter.Style.LONG,
    capitalizationContext: DisplayContext = DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE
) {
    private fun String.patternAsFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofPattern(this).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                localizedBy(locale)
            } else {
                withLocale(locale)
            }
        }.withZone(ZoneOffset.systemDefault())

    private var _dateFormat: DateTimeFormatter? = null
    private val dateFormat
        get() = _dateFormat ?: skeletons.createDatePattern(locale).patternAsFormatter().also { _dateFormat = it }

    private var _timeFormat: DateTimeFormatter? = null
    private val timeFormat
        get() = _timeFormat ?: skeletons.createTimePattern(locale).patternAsFormatter().also { _timeFormat = it }

    private var _dateTimeFormat: DateTimeFormatter? = null
    private val dateTimeFormat
        get() = _dateTimeFormat ?: skeletons.createDateTimePattern(locale).patternAsFormatter().also { _dateTimeFormat = it }

    private var _yearMonthFormat: DateTimeFormatter? = null
    private val yearMonthFormat
        get() = _yearMonthFormat ?: skeletons.createYearMonthPattern(locale).patternAsFormatter().also { _yearMonthFormat = it }

    private var _yearFormat: DateTimeFormatter? = null
    private val yearFormat
        get() = _yearFormat ?: skeletons.createYearPattern(locale).patternAsFormatter().also { _yearFormat = it }

    @Stable
    fun formatDate(temporal: Temporal): String = dateFormat.format(temporal)

    @Stable
    fun formatTime(temporal: Temporal): String = timeFormat.format(temporal)

    @Stable
    fun formatDateTime(temporal: Temporal): String = dateTimeFormat.format(temporal)

    @Stable
    fun formatYearMonth(temporal: Temporal): String = yearMonthFormat.format(temporal)

    @Stable
    fun formatYear(temporal: Temporal): String = yearFormat.format(temporal)

    private val relativeDateTimeFormatter =
        RelativeDateTimeFormatter.getInstance(ULocale.forLocale(locale), null, style, capitalizationContext)

    @Composable
    fun formatFancyAsState(
        temporal: Temporal
    ): State<String> {
        val state = rememberSaveable(temporal) {
            mutableStateOf(formatFancyInternal(temporal).first)
        }
        LaunchedEffect(temporal) {
            while (isActive) {
                val (formatted, delay) = formatFancyInternal(temporal)
                state.value = formatted
                delay(delay)
            }
        }
        return state
    }

    fun formatFancy(temporal: Temporal): String = formatFancyInternal(temporal).first

    private fun formatFancyInternal(
        temporal: Temporal
    ): Pair<String, Long> {
        val dayOffset = julianDayDistance(temporal)
        val millisOffset = temporal.epochMillis() - System.currentTimeMillis()
        val past = millisOffset < 0
        val secondsOffsetAbsolute = (millisOffset / 1000).absoluteValue

        if (secondsOffsetAbsolute < SWITCH_NOW_TO_SEC) {
            return relativeDateTimeFormatter.format(
                RelativeDateTimeFormatter.Direction.PLAIN,
                AbsoluteUnit.NOW
            ) to 1000
        }

        val date: String? = when (dayOffset) {
            -2L -> relativeDateTimeFormatter.format(RelativeDateTimeFormatter.Direction.LAST_2, AbsoluteUnit.DAY) ?: formatDate(temporal)
            -1L -> relativeDateTimeFormatter.format(RelativeDateTimeFormatter.Direction.LAST, AbsoluteUnit.DAY) ?: formatDate(temporal)

            0L -> if (secondsOffsetAbsolute < SWITCH_MIN_TO_TIME) {
                null
            } else {
                relativeDateTimeFormatter.format(RelativeDateTimeFormatter.Direction.THIS, AbsoluteUnit.DAY)
            }

            1L -> relativeDateTimeFormatter.format(RelativeDateTimeFormatter.Direction.NEXT, AbsoluteUnit.DAY) ?: formatDate(temporal)
            2L -> relativeDateTimeFormatter.format(RelativeDateTimeFormatter.Direction.NEXT_2, AbsoluteUnit.DAY) ?: formatDate(temporal)
            else -> formatDate(temporal)
        }

        val time = if (dayOffset == 0L) {
            val direction: RelativeDateTimeFormatter.Direction = if (past) {
                RelativeDateTimeFormatter.Direction.LAST
            } else {
                RelativeDateTimeFormatter.Direction.NEXT
            }

            when {
                secondsOffsetAbsolute < SWITCH_SEC_TO_MIN -> relativeDateTimeFormatter.format(
                    secondsOffsetAbsolute.toDouble(),
                    direction,
                    RelativeDateTimeFormatter.RelativeUnit.SECONDS
                )

                secondsOffsetAbsolute < SWITCH_MIN_TO_TIME -> relativeDateTimeFormatter.format(
                    secondsOffsetAbsolute.div(60).toDouble(),
                    direction,
                    RelativeDateTimeFormatter.RelativeUnit.MINUTES
                )

                else -> formatTime(temporal)
            }
        } else {
            formatTime(temporal)
        }

        val str = listOfNotNull(date, time).joinToString(dateTimeDivider)
        val delay = when {
            secondsOffsetAbsolute < SWITCH_SEC_TO_MIN -> 1_000L
            secondsOffsetAbsolute < SWITCH_MIN_TO_TIME -> 10_000L
            else -> 30_000L
        }
        return str to delay
    }

    companion object {
        private const val SWITCH_NOW_TO_SEC = 3L
        private const val SWITCH_SEC_TO_MIN = 60L * 2L
        private const val SWITCH_MIN_TO_TIME = 60L * 15L
    }
}

/**
 * **See spec** [tr35-dates](https://www.unicode.org/reports/tr35/tr35-dates.html#Date_Format_Patterns)
 */
@Immutable
data class FancyDateTimeFormatterSkeletons(
    val date: String = "ddMMyyyy",
    val time: String = "jj:mm",
    val dateTime: String = "ddMMyyyy jj:mm",
    val yearMonth: String = "yyyyMMM",
    val year: String = "yyyy"
) {
    fun createDatePattern(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, date)
    fun createTimePattern(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, time)
    fun createDateTimePattern(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, dateTime)
    fun createYearMonthPattern(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, yearMonth)
    fun createYearPattern(locale: Locale): String = DateFormat.getBestDateTimePattern(locale, year)
}

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun rememberFancyDateTimeFormatter(
    locale: Locale = LocalLocale.current,
    skeletons: FancyDateTimeFormatterSkeletons = FancyDateTimeFormatterSkeletons(),
    dateTimeDivider: String = ", ",
    style: RelativeDateTimeFormatter.Style = RelativeDateTimeFormatter.Style.LONG,
    capitalizationContext: DisplayContext = DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE
): FancyDateTimeFormatter {
    return remember(locale, skeletons, dateTimeDivider, style, capitalizationContext) {
        FancyDateTimeFormatter(
            locale = locale,
            skeletons = skeletons,
            dateTimeDivider = dateTimeDivider,
            style = style,
            capitalizationContext = capitalizationContext
        )
    }
}

@Preview(showBackground = true)
@RequiresApi(Build.VERSION_CODES.N)
@Composable
private fun FancyDateTimeFormatterPreview() {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val formatter = rememberFancyDateTimeFormatter()
            val now = LocalDateTime.now()
            val dates = remember(now) {
                listOf(
                    now.minusDays(3),
                    now.minusDays(2),
                    now.minusDays(1),
                    now.minusHours(1),
                    now.minusMinutes(5),
                    now.minusSeconds(30),
                    now,
                    now.plusSeconds(30),
                    now.plusMinutes(5),
                    now.plusHours(1),
                    now.plusDays(1),
                    now.plusDays(2),
                    now.plusDays(3),
                )
            }

            dates.fastForEach {
                Text(text = formatter.formatFancy(it))
            }

            HorizontalDivider()

            Text(
                text = "formatDate> " + formatter.formatDate(now)
            )
            Text(
                text = "formatTime> " + formatter.formatTime(now)
            )
            Text(
                text = "formatDateTime> " + formatter.formatDateTime(now)
            )
            Text(
                text = "formatYearMonth> " + formatter.formatYearMonth(now)
            )
            Text(
                text = "formatYear> " + formatter.formatYear(now)
            )
        }
    }
}
