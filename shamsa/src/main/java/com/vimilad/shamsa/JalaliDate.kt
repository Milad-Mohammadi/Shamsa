package com.vimilad.shamsa

import java.util.Calendar
import java.util.Date

/** A validated Jalali date. Construction throws [IllegalArgumentException] for impossible dates. */
data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    init {
        require(month in 1..12) { "Invalid month: $month" }
        require(day in 1..JalaliCalendar.monthLength(year, month)) {
            "Invalid day $day for $year/$month"
        }
    }

    /** Midnight (00:00:00) of this day in the device time zone. */
    fun toDate(): Date {
        val (gy, gm, gd) = JalaliCalendar.toGregorian(year, month, day)
        return Calendar.getInstance().apply {
            clear()
            set(gy, gm - 1, gd, 0, 0, 0)
        }.time
    }

    override fun toString(): String = "%04d/%02d/%02d".format(year, month, day)

    companion object {
        fun today(): JalaliDate = from(Date())

        fun from(date: Date): JalaliDate {
            val c = Calendar.getInstance().apply { time = date }
            return fromGregorian(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        }

        fun fromMillis(millis: Long): JalaliDate = from(Date(millis))

        fun fromGregorian(year: Int, month: Int, day: Int): JalaliDate {
            val (jy, jm, jd) = JalaliCalendar.fromGregorian(year, month, day)
            return JalaliDate(jy, jm, jd)
        }
    }
}
