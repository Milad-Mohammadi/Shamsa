package com.vimilad.shamsa

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

/**
 * Checks the calendar against reference data produced by the samanzamani PersianDate library
 * (see golden-jalali-years.csv): for every Jalali year 1178..1633 the leap flag and the Gregorian
 * date of 1 Farvardin.
 *
 * Every other day is then verified with plain JDK date arithmetic (adding one day at a time to
 * the reference new-year date), which does not depend on this library's own conversion code.
 */
class GoldenCalendarTest {

    private class Row(val year: Int, val leap: Boolean, val gregorian: IntArray)

    private val rows: List<Row> by lazy {
        val stream = javaClass.getResourceAsStream("/golden-jalali-years.csv")
        requireNotNull(stream) { "golden-jalali-years.csv not found on the test classpath" }
        stream.bufferedReader().readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { line ->
                val p = line.split(",").map { it.trim().toInt() }
                Row(p[0], p[1] == 1, intArrayOf(p[2], p[3], p[4]))
            }
    }

    private fun utcCalendar(g: IntArray) = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(g[0], g[1] - 1, g[2], 0, 0, 0)
    }

    private fun ymd(c: Calendar) = intArrayOf(
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
    )

    @Test
    fun referenceHasExpectedSize() {
        assertEquals(1633 - 1178 + 1, rows.size)
        assertEquals(1178, rows.first().year)
        assertEquals(1633, rows.last().year)
    }

    @Test
    fun leapFlagsMatchReference() {
        for (r in rows) assertEquals("leap ${r.year}", r.leap, JalaliCalendar.isLeapYear(r.year))
    }

    @Test
    fun newYearDatesMatchReference() {
        for (r in rows) {
            assertArrayEquals("1 Farvardin ${r.year}", r.gregorian, JalaliCalendar.toGregorian(r.year, 1, 1))
            assertArrayEquals("back ${r.year}", intArrayOf(r.year, 1, 1), JalaliCalendar.fromGregorian(r.gregorian[0], r.gregorian[1], r.gregorian[2]))
        }
    }

    @Test
    fun yearLengthsMatchDistanceBetweenNewYears() {
        for (i in 0 until rows.size - 1) {
            val a = utcCalendar(rows[i].gregorian)
            val b = utcCalendar(rows[i + 1].gregorian)
            val days = (b.timeInMillis - a.timeInMillis) / 86_400_000L
            val expected = if (rows[i].leap) 366L else 365L
            assertEquals("length of ${rows[i].year}", expected, days)
        }
    }

    @Test
    fun monthLengthsAddUpToYearLength() {
        for (r in rows) {
            val total = (1..12).sumOf { JalaliCalendar.monthLength(r.year, it) }
            assertEquals("days in ${r.year}", if (r.leap) 366 else 365, total)
        }
    }

    @Test
    fun everyDayMatchesIndependentDayCount() {
        for (i in rows.indices) {
            val r = rows[i]
            val cal = utcCalendar(r.gregorian)
            for (m in 1..12) for (d in 1..JalaliCalendar.monthLength(r.year, m)) {
                val expected = ymd(cal)
                assertArrayEquals("${r.year}/$m/$d -> gregorian", expected, JalaliCalendar.toGregorian(r.year, m, d))
                assertArrayEquals(
                    "$expected -> ${r.year}/$m/$d",
                    intArrayOf(r.year, m, d),
                    JalaliCalendar.fromGregorian(expected[0], expected[1], expected[2]),
                )
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            // After the last day of the year we must be exactly on the next new year.
            if (i + 1 < rows.size) {
                assertTrue("end of ${r.year}", ymd(cal).contentEquals(rows[i + 1].gregorian))
            }
        }
    }
}
