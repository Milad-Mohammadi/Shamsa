package com.vimilad.shamsa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ShamsaTypesTest {

    @Test
    fun gregorianLeapRule() {
        assertTrue(ShamsaDate.isLeapYear(ShamsaCalendar.Gregorian, 2000))
        assertTrue(ShamsaDate.isLeapYear(ShamsaCalendar.Gregorian, 2024))
        assertFalse(ShamsaDate.isLeapYear(ShamsaCalendar.Gregorian, 1900))
        assertFalse(ShamsaDate.isLeapYear(ShamsaCalendar.Gregorian, 2100))
        assertFalse(ShamsaDate.isLeapYear(ShamsaCalendar.Gregorian, 2025))
    }

    @Test
    fun gregorianMonthLengths() {
        val g = ShamsaCalendar.Gregorian
        listOf(1, 3, 5, 7, 8, 10, 12).forEach { assertEquals(31, ShamsaDate.monthLength(g, 2025, it)) }
        listOf(4, 6, 9, 11).forEach { assertEquals(30, ShamsaDate.monthLength(g, 2025, it)) }
        assertEquals(28, ShamsaDate.monthLength(g, 2025, 2))
        assertEquals(29, ShamsaDate.monthLength(g, 2024, 2))
        assertEquals(28, ShamsaDate.monthLength(g, 1900, 2))
        assertEquals(29, ShamsaDate.monthLength(g, 2000, 2))
    }

    @Test
    fun jalaliMonthLengthsThroughShamsaDate() {
        val j = ShamsaCalendar.Jalali
        assertEquals(31, ShamsaDate.monthLength(j, 1405, 6))
        assertEquals(30, ShamsaDate.monthLength(j, 1405, 7))
        assertEquals(29, ShamsaDate.monthLength(j, 1404, 12))
        assertEquals(30, ShamsaDate.monthLength(j, 1403, 12))
    }

    @Test
    fun invalidDatesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.jalali(1404, 12, 30) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.jalali(1404, 7, 31) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.jalali(1404, 13, 1) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.jalali(1404, 1, 0) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.gregorian(2025, 2, 29) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaDate.gregorian(2025, 4, 31) }
        ShamsaDate.jalali(1403, 12, 30)   // leap year: must not throw
        ShamsaDate.gregorian(2024, 2, 29) // leap year: must not throw
    }

    @Test
    fun defaultCalendarIsJalali() {
        assertEquals(ShamsaCalendar.Jalali, ShamsaDate(1405, 8, 12).calendar)
    }

    @Test
    fun conversionsBetweenCalendars() {
        val j = ShamsaDate.jalali(1405, 8, 12)
        assertEquals(ShamsaDate.gregorian(2026, 11, 3), j.toGregorian())
        assertEquals(j, ShamsaDate.gregorian(2026, 11, 3).toJalali())
        assertEquals(ShamsaDate.jalali(1402, 12, 10), ShamsaDate.gregorian(2024, 2, 29).toJalali())
        assertEquals(ShamsaDate.gregorian(2025, 3, 20), ShamsaDate.jalali(1403, 12, 30).toGregorian())
        assertSame(j, j.convertTo(ShamsaCalendar.Jalali))
    }

    @Test
    fun conversionRoundTripEveryDayOfTheDefaultRanges() {
        for (y in 1300..1500) for (m in 1..12) for (d in 1..ShamsaDate.monthLength(ShamsaCalendar.Jalali, y, m)) {
            val j = ShamsaDate.jalali(y, m, d)
            assertEquals(j, j.toGregorian().toJalali())
        }
    }

    @Test
    fun toDateIsLocalMidnightOfTheGregorianDay() {
        val c = Calendar.getInstance().apply { time = ShamsaDate.jalali(1405, 8, 12).toDate() }
        assertEquals(2026, c.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH))
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, c.get(Calendar.MINUTE))
        assertEquals(0, c.get(Calendar.SECOND))
    }

    @Test
    fun fromDateRespectsRequestedCalendar() {
        val date = ShamsaDate.gregorian(2026, 11, 3).toDate()
        assertEquals(ShamsaDate.jalali(1405, 8, 12), ShamsaDate.from(date))
        assertEquals(ShamsaDate.gregorian(2026, 11, 3), ShamsaDate.from(date, ShamsaCalendar.Gregorian))
    }

    @Test
    fun toStringUsesLatinDigitsAndPadding() {
        assertEquals("1405/08/12", ShamsaDate.jalali(1405, 8, 12).toString())
        assertEquals("2026/01/05", ShamsaDate.gregorian(2026, 1, 5).toString())
    }

    @Test
    fun timeValidation() {
        ShamsaTime(0, 0, 0)
        ShamsaTime(23, 59, 59)
        assertThrows(IllegalArgumentException::class.java) { ShamsaTime(24, 0) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaTime(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaTime(10, 60) }
        assertThrows(IllegalArgumentException::class.java) { ShamsaTime(10, 0, 60) }
        assertEquals("07:05:00", ShamsaTime(7, 5).toString())
    }

    @Test
    fun dateTimeToDateCombinesDateAndTime() {
        val value = ShamsaDateTime(ShamsaDate.jalali(1405, 8, 12), ShamsaTime(14, 35, 9))
        val c = Calendar.getInstance().apply { time = value.toDate() }
        assertEquals(2026, c.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH))
        assertEquals(3, c.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY))
        assertEquals(35, c.get(Calendar.MINUTE))
        assertEquals(9, c.get(Calendar.SECOND))
        assertEquals("1405/08/12 14:35:09", value.toString())
    }
}
