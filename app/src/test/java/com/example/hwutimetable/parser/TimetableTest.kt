package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class TimetableTest {
    private val hash = ByteArray(1) { 0 }

    @Test
    fun testEmptyTimetable() {
        val timetable = Timetable(hash, arrayOf(), Semester(LocalDate.now()))
        assertEquals("Total items is 0?", 0, timetable.getTotalItems())
    }

    @Test
    fun testNotEmptyTimetable() {
        val timetable = Timetable(
            hash, arrayOf(
                createTimetableDay(Day.MONDAY, 1),
                createTimetableDay(Day.TUESDAY, 2),
                createTimetableDay(Day.WEDNESDAY, 3),
                createTimetableDay(Day.THURSDAY, 4),
                createTimetableDay(Day.FRIDAY, 5)
            ),
            Semester(LocalDate.now())
        )

        val expectedCount = (1..5).sum()
        assertEquals(expectedCount, timetable.getTotalItems())
    }

    @Test
    fun testNoClashDifferentTimes() {
        val timetable = Timetable(
            hash, arrayOf(
                TimetableDay(
                    Day.MONDAY, arrayListOf(
                        createTimetableItem(
                            LocalTime(9, 15),
                            LocalTime(10, 15),
                            WeeksBuilder().setRange(1, 1).getWeeks()
                        ),
                        createTimetableItem(
                            LocalTime(10, 15),
                            LocalTime(11, 15),
                            WeeksBuilder().setRange(1, 1).getWeeks()
                        )
                    )
                )
            ),
            Semester(LocalDate.now())
        )
        assertTrue(timetable.getClashes().isEmpty())
    }

    @Test
    fun testNoClashDifferentWeeks() {
        val timetable = Timetable(
            hash, arrayOf(
                TimetableDay(
                    Day.MONDAY, arrayListOf(
                        createTimetableItem(
                            LocalTime(9, 15),
                            LocalTime(10, 15),
                            WeeksBuilder()
                                .setRange(1, 7)
                                .getWeeks()
                        ),
                        createTimetableItem(
                            LocalTime(9, 15),
                            LocalTime(10, 15),
                            WeeksBuilder()
                                .setRange(8, 12)
                                .getWeeks()
                        )
                    )
                )
            ),
            Semester(LocalDate.now())
        )

        assertTrue(timetable.getClashes().isEmpty())
    }

    @Test
    fun testClash() {
        val timetable = Timetable(
            hash, arrayOf(
                TimetableDay(
                    Day.MONDAY, arrayListOf(
                        createTimetableItem(
                            LocalTime(9, 15),
                            LocalTime(10, 15),
                            WeeksBuilder()
                                .setRange(1, 7)
                                .getWeeks()
                        ),
                        createTimetableItem(
                            LocalTime(9, 15),
                            LocalTime(10, 15),
                            WeeksBuilder()
                                .setRange(4, 12)
                                .getWeeks()
                        )
                    )
                )
            ),
            Semester(LocalDate.now())
        )

        assertFalse(timetable.getClashes().isEmpty())
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

    private fun createTimetableItem(): TimetableItem {
        return createTimetableItem(
            LocalTime.MIDNIGHT, LocalTime.MIDNIGHT,
            WeeksBuilder().setRange(1, 1).getWeeks()
        )
    }

    private fun createTimetableItem(startTime: LocalTime, endTime: LocalTime, weeks: Weeks): TimetableItem {
        return TimetableItem(
            "code", "name", "room", "lecturer", ItemType("type"),
            startTime, endTime, weeks
        )
    }
}