package com.example.hwutimetable.parser

import org.joda.time.LocalTime
import org.joda.time.Period
import org.junit.Assert.assertEquals
import org.junit.Test

class TimetableTest {
    private val hash = ByteArray(1) { 0 }

    @Test
    fun testEmptyTimetable() {
        val timetable = Timetable(hash, arrayOf())
        assertEquals("Total items is 0?", 0, timetable.getTotalItems())
    }

    @Test
    fun testNotEmptyTimetable() {
        val timetable = Timetable(hash, arrayOf(
            createTimetableDay(Day.MONDAY, 1),
            createTimetableDay(Day.TUESDAY, 2),
            createTimetableDay(Day.WEDNESDAY, 3),
            createTimetableDay(Day.THURSDAY, 4),
            createTimetableDay(Day.FRIDAY, 5)
        ))

        val expectedCount = (1..5).sum()
        assertEquals(expectedCount, timetable.getTotalItems())
    }

    private fun createTimetableDay(day: Day, itemsInDay: Int): TimetableDay {
        return TimetableDay(day, createTimetableItems(itemsInDay))
    }

    private fun createTimetableItems(size: Int): ArrayList<TimetableItem> {
        val list = mutableListOf<TimetableItem>()
        for (i in 1..size) {
            list.add(createTimetableItem())
        }
        return ArrayList(list)
    }

    private fun createTimetableItem() :  TimetableItem {
        return TimetableItem("code", "name", "room" ,"lecturer", ItemType("type"),
            LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, "1", Period.ZERO)
    }
}