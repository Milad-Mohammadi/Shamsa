package com.vimilad.shamsa

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliCalendarTest {

    @Test
    fun leapYears() {
        listOf(1395, 1399, 1403, 1408).forEach { assertTrue("$it", JalaliCalendar.isLeapYear(it)) }
        listOf(1400, 1401, 1402, 1404, 1405, 1406, 1407).forEach {
            assertFalse("$it", JalaliCalendar.isLeapYear(it))
        }
    }

    @Test
    fun monthLengths() {
        for (m in 1..6) assertEquals(31, JalaliCalendar.monthLength(1404, m))
        for (m in 7..11) assertEquals(30, JalaliCalendar.monthLength(1404, m))
        assertEquals(30, JalaliCalendar.monthLength(1403, 12)) // leap year
        assertEquals(29, JalaliCalendar.monthLength(1404, 12))
    }

    @Test
    fun invalidDateRejected() {
        assertThrows(IllegalArgumentException::class.java) { JalaliDate(1404, 12, 30) }
        assertThrows(IllegalArgumentException::class.java) { JalaliDate(1404, 7, 31) }
        assertThrows(IllegalArgumentException::class.java) { JalaliDate(1404, 13, 1) }
        JalaliDate(1403, 12, 30) // must not throw
    }

    @Test
    fun conversionKnownDates() {
        assertArrayEquals(intArrayOf(2026, 11, 3), JalaliCalendar.toGregorian(1405, 8, 12))
        assertArrayEquals(intArrayOf(2024, 3, 20), JalaliCalendar.toGregorian(1403, 1, 1))
        assertArrayEquals(intArrayOf(2025, 3, 20), JalaliCalendar.toGregorian(1403, 12, 30))
        assertArrayEquals(intArrayOf(1405, 8, 12), JalaliCalendar.fromGregorian(2026, 11, 3))
    }

    @Test
    fun roundTripEveryDay() {
        for (y in 1300..1500) for (m in 1..12) for (d in 1..JalaliCalendar.monthLength(y, m)) {
            val g = JalaliCalendar.toGregorian(y, m, d)
            val j = JalaliCalendar.fromGregorian(g[0], g[1], g[2])
            assertArrayEquals("$y/$m/$d", intArrayOf(y, m, d), j)
        }
    }
}
