package com.vimilad.shamsa

/**
 * Jalali (Persian) calendar math with no external dependencies.
 *
 * Algorithm: jalaali-js (Behrang Noruzi Niya), a break-point table approach that
 * matches the official Iranian calendar for years 1178..1633 AP.
 */
object JalaliCalendar {

    const val MIN_YEAR = -61
    const val MAX_YEAR = 3177

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private class Cal(val leap: Int, val gy: Int, val march: Int)

    private fun calc(jy: Int): Cal {
        require(jy in MIN_YEAR..MAX_YEAR) { "Jalali year out of range: $jy" }
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += (jump / 33) * 8 + (jump % 33) / 4
            jp = jm
        }
        var n = jy - jp
        leapJ += (n / 33) * 8 + ((n % 33) + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1

        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG

        if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return Cal(leap, gy, march)
    }

    /** Returns true if [year] is a Jalali leap year. */
    fun isLeapYear(year: Int): Boolean = calc(year).leap == 0

    /** Months 1..6 have 31 days, 7..11 have 30, and Esfand has 29 (30 in a leap year). */
    fun monthLength(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapYear(year)) 30 else 29
        else -> throw IllegalArgumentException("month must be in 1..12 but was $month")
    }

    /** Jalali to Gregorian. Returns [year, month, day]. */
    fun toGregorian(jy: Int, jm: Int, jd: Int): IntArray {
        val r = calc(jy)
        val jdn = g2d(r.gy, 3, r.march) + (jm - 1) * 31 - (jm / 7) * (jm - 7) + jd - 1
        return d2g(jdn)
    }

    /** Gregorian to Jalali. Returns [year, month, day]. */
    fun fromGregorian(gy: Int, gm: Int, gd: Int): IntArray = d2j(g2d(gy, gm, gd))

    private fun d2j(jdn: Int): IntArray {
        val gy = d2g(jdn)[0]
        var jy = gy - 621
        val r = calc(jy)
        val jdn1f = g2d(gy, 3, r.march)
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) return intArrayOf(jy, 1 + k / 31, k % 31 + 1)
            k -= 186
        } else {
            jy -= 1
            k += 179
            if (r.leap == 1) k += 1
        }
        return intArrayOf(jy, 7 + k / 30, k % 30 + 1)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = ((gy + (gm - 8) / 6 + 100100) * 1461) / 4 +
            (153 * ((gm + 9) % 12) + 2) / 5 + gd - 34840408
        d = d - (((gy + 100100 + (gm - 8) / 6) / 100) * 3) / 4 + 752
        return d
    }

    private fun d2g(jdn: Int): IntArray {
        var j = 4 * jdn + 139361631
        j += (((4 * jdn + 183187720) / 146097) * 3) / 4 * 4 - 3908
        val i = ((j % 1461) / 4) * 5 + 308
        val gd = (i % 153) / 5 + 1
        val gm = (i / 153) % 12 + 1
        val gy = j / 1461 - 100100 + (8 - gm) / 6
        return intArrayOf(gy, gm, gd)
    }
}
