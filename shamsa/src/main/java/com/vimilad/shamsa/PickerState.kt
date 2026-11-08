package com.vimilad.shamsa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

// ---------------------------------------------------------------- date

internal class DateFields(
    val calendar: ShamsaCalendar,
    val year: MutableIntState,
    val month: MutableIntState,
    val day: MutableIntState,
) {
    fun monthLength(): Int = ShamsaDate.monthLength(calendar, year.intValue, month.intValue)

    fun toDate(): ShamsaDate = ShamsaDate(
        year.intValue, month.intValue, day.intValue.coerceAtMost(monthLength()), calendar
    )
}

@Composable
internal fun rememberDateFields(
    initial: ShamsaDate,
    calendar: ShamsaCalendar,
    yearRange: IntRange,
): DateFields {
    require(!yearRange.isEmpty()) { "yearRange must not be empty" }
    val start = initial.convertTo(calendar)
    val year = rememberSaveable { mutableIntStateOf(start.year.coerceIn(yearRange)) }
    val month = rememberSaveable { mutableIntStateOf(start.month) }
    val day = rememberSaveable { mutableIntStateOf(start.day) }
    return remember(calendar) { DateFields(calendar, year, month, day) }
}

/** Day | month | year wheels. The day is clamped when the month or year makes it too large. */
@Composable
internal fun rememberDateColumns(
    fields: DateFields,
    yearRange: IntRange,
    texts: ShamsaTexts,
): List<WheelColumn> {
    val monthLength = fields.monthLength()
    // e.g. 31 Mehr -> Aban becomes 30, 30 Esfand of a leap year -> a normal year becomes 29.
    LaunchedEffect(monthLength) {
        fields.day.intValue = fields.day.intValue.coerceAtMost(fields.monthLength())
    }
    val persian = texts.usePersianDigits
    val years = remember(yearRange, persian) { yearRange.map { it.localized(persian) } }
    val days = remember(monthLength, persian) { (1..monthLength).map { it.localized(persian) } }
    return listOf(
        WheelColumn(
            items = days,
            index = { fields.day.intValue.coerceAtMost(fields.monthLength()) - 1 },
            onSelect = { fields.day.intValue = it + 1 },
        ),
        WheelColumn(
            items = texts.monthNames,
            index = { fields.month.intValue - 1 },
            onSelect = { fields.month.intValue = it + 1 },
            weight = 1.4f,
        ),
        WheelColumn(
            items = years,
            index = { fields.year.intValue - yearRange.first },
            onSelect = { fields.year.intValue = yearRange.first + it },
        ),
    )
}

// ---------------------------------------------------------------- time

internal class TimeFields(
    val hour: MutableIntState,
    val minute: MutableIntState,
    val second: MutableIntState,
) {
    fun toTime(showSeconds: Boolean, minuteStep: Int): ShamsaTime = ShamsaTime(
        hour.intValue,
        minute.intValue / minuteStep * minuteStep,
        if (showSeconds) second.intValue else 0,
    )
}

@Composable
internal fun rememberTimeFields(initial: ShamsaTime): TimeFields {
    val hour = rememberSaveable { mutableIntStateOf(initial.hour) }
    val minute = rememberSaveable { mutableIntStateOf(initial.minute) }
    val second = rememberSaveable { mutableIntStateOf(initial.second) }
    return remember { TimeFields(hour, minute, second) }
}

/** Hour | minute (| second) wheels. */
@Composable
internal fun rememberTimeColumns(
    fields: TimeFields,
    showSeconds: Boolean,
    minuteStep: Int,
    persianDigits: Boolean,
): List<WheelColumn> {
    require(minuteStep in 1..30) { "minuteStep must be in 1..30" }
    val hours = remember(persianDigits) { (0..23).map { it.localized(persianDigits, 2) } }
    val minutes = remember(minuteStep, persianDigits) {
        (0 until 60 step minuteStep).map { it.localized(persianDigits, 2) }
    }
    val seconds = remember(persianDigits) { (0..59).map { it.localized(persianDigits, 2) } }
    return buildList {
        add(WheelColumn(hours, { fields.hour.intValue }, { fields.hour.intValue = it }))
        add(
            WheelColumn(
                minutes,
                { fields.minute.intValue / minuteStep },
                { fields.minute.intValue = it * minuteStep },
            )
        )
        if (showSeconds) add(WheelColumn(seconds, { fields.second.intValue }, { fields.second.intValue = it }))
    }
}
