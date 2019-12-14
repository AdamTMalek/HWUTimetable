package com.example.hwutimetable.parser

import com.example.hwutimetable.extensions.toIntArray
import com.example.hwutimetable.parser.exceptions.InvalidRangeException
import org.junit.Assert.assertTrue
import org.junit.Test

class TestWeeksBuilder {
    @Test
    fun testSetRange() {
        val weeks = WeeksBuilder()
            .setRange(1, 12)
            .getNumberOfWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidStartRange() {
        WeeksBuilder()
            .setRange(-2, 4)
            .getNumberOfWeeks()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidEndRange() {
        WeeksBuilder()
            .setRange(1, -1)
            .getNumberOfWeeks()
    }

    @Test(expected = InvalidRangeException::class)
    fun testSetInvalidRange() {
        WeeksBuilder()
            .setRange(6, 3)
            .getNumberOfWeeks()
    }

    @Test
    fun testSetWeeks() {
        val expected = (1..12).toIntArray()
        val weeks = WeeksBuilder()
            .setWeeks(expected)
            .getNumberOfWeeks()

        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetInvalidWeeks() {
        val weeks = (-2..5).toIntArray()
        WeeksBuilder()
            .setWeeks(weeks)
            .getNumberOfWeeks()
    }

    @Test
    fun testSetRangeFromString() {
        val weeks = WeeksBuilder()
            .setFromString("1-12")
            .getNumberOfWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }

    @Test
    fun testSetListFromString() {
        val weeks = WeeksBuilder()
            .setFromString("1,2,3,4,5,6,7,8,9,10,11,12")
            .getNumberOfWeeks()

        val expected = (1..12).toIntArray()
        assertTrue(expected.contentEquals(weeks.weeks))
    }
}
