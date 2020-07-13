package com.github.hwutimetable.parser

import com.github.hwutimetable.extensions.toIntArray
import com.github.hwutimetable.parser.exceptions.InvalidRangeException
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeksBuilderTest {
    @Test
    fun testSetRange() {
        val weeks = WeeksBuilder()
            .setRange(1, 12)
            .getWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidStartRange() {
        WeeksBuilder()
            .setRange(-2, 4)
            .getWeeks()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidEndRange() {
        WeeksBuilder()
            .setRange(1, -1)
            .getWeeks()
    }

    @Test(expected = InvalidRangeException::class)
    fun testSetInvalidRange() {
        WeeksBuilder()
            .setRange(6, 3)
            .getWeeks()
    }

    @Test
    fun testSetWeeks() {
        val expected = (1..12).toIntArray()
        val weeks = WeeksBuilder()
            .setWeeks(expected)
            .getWeeks()

        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidWeeks() {
        val weeks = (-2..5).toIntArray()
        WeeksBuilder()
            .setWeeks(weeks)
            .getWeeks()
    }

    @Test
    fun testSetRangeFromString() {
        val weeks = WeeksBuilder()
            .setFromString("1-12")
            .getWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test
    fun testSetListFromString() {
        val weeks = WeeksBuilder()
            .setFromString("1,2,3,4,5,6,7,8,9,10,11,12")
            .getWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }
}
