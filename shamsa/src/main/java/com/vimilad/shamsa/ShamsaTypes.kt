package com.vimilad.shamsa

import java.util.Calendar
import java.util.Date
import java.util.Locale

/** The calendar system a date is expressed in. */
enum class ShamsaCalendar { Jalali, Gregorian }

/**
 * A validated date in either the Jalali or the Gregorian calendar.
 * Construction throws [IllegalArgumentException] for impossible dates (e.g. 30 Esfand 1404).
 */
data class ShamsaDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val calendar: ShamsaCalendar = ShamsaCalendar.Jalali,
) {
    init {
        require(month in 1..12) { "Invalid month: $month" }
        require(day in 1..monthLength(calendar, year, month)) {
            "Invalid day $day for $year/$month ($calendar)"
        }
    }

    fun toJalali(): ShamsaDate = convertTo(ShamsaCalendar.Jalali)

    fun toGregorian(): ShamsaDate = convertTo(ShamsaCalendar.Gregorian)

    fun convertTo(target: ShamsaCalendar): ShamsaDate {
        if (target == calendar) return this
        val r = when (target) {
            ShamsaCalendar.Gregorian -> JalaliCalendar.toGregorian(year, month, day)
            ShamsaCalendar.Jalali -> JalaliCalendar.fromGregorian(year, month, day)
        }
        return ShamsaDate(r[0], r[1], r[2], target)
    }

    /** Midnight (00:00:00) of this day in the device time zone. */
    fun toDate(): Date {
        val g = toGregorian()
        return Calendar.getInstance().apply {
            clear()
            set(g.year, g.month - 1, g.day, 0, 0, 0)
        }.time
    }

    override fun toString(): String = String.format(Locale.ROOT, "%04d/%02d/%02d", year, month, day)

    companion object {
        fun jalali(year: Int, month: Int, day: Int) = ShamsaDate(year, month, day, ShamsaCalendar.Jalali)

        fun gregorian(year: Int, month: Int, day: Int) = ShamsaDate(year, month, day, ShamsaCalendar.Gregorian)

        fun today(calendar: ShamsaCalendar = ShamsaCalendar.Jalali): ShamsaDate = from(Date(), calendar)

        fun from(date: Date, calendar: ShamsaCalendar = ShamsaCalendar.Jalali): ShamsaDate {
            val c = Calendar.getInstance().apply { time = date }
            return gregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
                .convertTo(calendar)
        }

        fun fromMillis(millis: Long, calendar: ShamsaCalendar = ShamsaCalendar.Jalali): ShamsaDate =
            from(Date(millis), calendar)

        fun isLeapYear(calendar: ShamsaCalendar, year: Int): Boolean = when (calendar) {
            ShamsaCalendar.Jalali -> JalaliCalendar.isLeapYear(year)
            ShamsaCalendar.Gregorian -> year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        }

        fun monthLength(calendar: ShamsaCalendar, year: Int, month: Int): Int {
            require(month in 1..12) { "month must be in 1..12 but was $month" }
            return when (calendar) {
                ShamsaCalendar.Jalali -> JalaliCalendar.monthLength(year, month)
                ShamsaCalendar.Gregorian -> when (month) {
                    2 -> if (isLeapYear(calendar, year)) 29 else 28
                    4, 6, 9, 11 -> 30
                    else -> 31
                }
            }
        }
    }
}

/** A 24-hour time of day. */
data class ShamsaTime(val hour: Int, val minute: Int, val second: Int = 0) {
    init {
        require(hour in 0..23) { "Invalid hour: $hour" }
        require(minute in 0..59) { "Invalid minute: $minute" }
        require(second in 0..59) { "Invalid second: $second" }
    }

    override fun toString(): String = String.format(Locale.ROOT, "%02d:%02d:%02d", hour, minute, second)

    companion object {
        fun now(): ShamsaTime {
            val c = Calendar.getInstance()
            return ShamsaTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND))
        }
    }
}

/** A date followed by a time of day. */
data class ShamsaDateTime(val date: ShamsaDate, val time: ShamsaTime) {

    /** The instant of this date and time in the device time zone. */
    fun toDate(): Date {
        val g = date.toGregorian()
        val t = time // `time` would otherwise resolve to Calendar.time inside apply { }
        return Calendar.getInstance().apply {
            clear()
            set(g.year, g.month - 1, g.day, t.hour, t.minute, t.second)
        }.time
    }

    override fun toString(): String = "$date $time"

    companion object {
        fun now(calendar: ShamsaCalendar = ShamsaCalendar.Jalali): ShamsaDateTime =
            ShamsaDateTime(ShamsaDate.today(calendar), ShamsaTime.now())
    }
}
