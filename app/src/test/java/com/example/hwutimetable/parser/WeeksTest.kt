package com.example.hwutimetable.parser

import com.example.hwutimetable.extensions.toIntArray
import org.junit.Assert.*
import org.junit.Test

class WeeksTest {
    @Test
    fun testDurationOneWeek() {
        val weeks = Weeks(intArrayOf(1))
        assertEquals(1, weeks.getNumberOfWeeks())
    }

    @Test
    fun testDuration() {
        val weeks = Weeks(intArrayOf(1, 2, 3, 4, 5))
        assertEquals(5, weeks.getNumberOfWeeks())
    }

    @Test
    fun testNoCommon() {
        val a = Weeks(intArrayOf(1, 2, 3))
        val b = Weeks(intArrayOf(4, 5, 6))

        assertFalse(a.hasCommon(b))
    }

    @Test
    fun testCommon() {
        val a = Weeks(intArrayOf(1, 2, 3))
        val b = Weeks(intArrayOf(3, 4, 5))

        assertTrue(a.hasCommon(b))
    }

    @Test
    fun testToStringRangeable() {
        val weeks = Weeks((3..7).toIntArray())
        val expected = "3-7"

        assertEquals(expected, weeks.toString())
    }

    @Test
    fun testToStringNotRangeable() {
        val weeks = Weeks(intArrayOf(1, 3, 6))
        val expected = "1,3,6"

        assertEquals(expected, weeks.toString())
    }
}