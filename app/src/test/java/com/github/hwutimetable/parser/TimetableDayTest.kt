package com.github.hwutimetable.parser

import org.joda.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TimetableDayTest {
    @Test
    fun `test if clash is detected for the same times`() {
        val startTime = LocalTime(9, 15)
        val endTime = LocalTime(10, 15)
        val item1 = createTimetableItem(startTime, endTime, WeeksBuilder().setRange(1, 7).getWeeks())
        val item2 = createTimetableItem(startTime, endTime, WeeksBuilder().setRange(7, 12).getWeeks())
        val day = TimetableDay(Day.MONDAY, arrayListOf(item1, item2))

        assertFalse(day.getClashes(7).isEmpty())
    }

    @Test
    fun `test if clash is detected for overlapping times`() {
        val item1 = createTimetableItem(
            LocalTime(10, 15),
            LocalTime(11, 15),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )
        val item2 = createTimetableItem(
            LocalTime(9, 15),
            LocalTime(10, 30),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )
        val item3 = createTimetableItem(
            LocalTime(10, 30),
            LocalTime(11, 30),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )

        val day = TimetableDay(Day.MONDAY, arrayListOf(item1, item2, item3))
        assertEquals(2, day.getClashes(7).getClashes().size)
    }

    @Test
    fun `test there is no clash with different times`() {
        val item1 = createTimetableItem(
            LocalTime(10, 15),
            LocalTime(11, 15),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )
        val item2 = createTimetableItem(
            LocalTime(9, 15),
            LocalTime(10, 15),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )
        val item3 = createTimetableItem(
            LocalTime(11, 15),
            LocalTime(11, 30),
            WeeksBuilder().setRange(1, 7).getWeeks()
        )

        val day = TimetableDay(Day.MONDAY, arrayListOf(item1, item2, item3))
        assertEquals(0, day.getClashes(7).getClashes().size)
    }

    private fun createTimetableItem(startTime: LocalTime, endTime: LocalTime, weeks: Weeks): TimetableItem {
        return TimetableItem(
            "xxx", "xxx", "xxx", "xxx", ItemType("xxx"),
            startTime, endTime, weeks
        )
    }
}