package com.github.hwutimetable.parser

import com.github.hwutimetable.SampleTimetableHandler
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class TimetableTest {
    private val timetableHandler = SampleTimetableHandler()

    @Test
    fun testEmptyTimetable() {
        val timetable = Timetable(
            arrayOf(),
            Timetable.Info(
                "C01", "Test Timetable", Semester(LocalDate.now(), 1)
            )
        )

        assertEquals(
            "Total number of classes is 0?", 0, timetable.getClassesCount()
        )
    }

    @Test
    fun testNotEmptyTimetable() {
        val timetable = Timetable(
            arrayOf(
                createTimetableDay(Day.MONDAY, 1),
                createTimetableDay(Day.TUESDAY, 2),
                createTimetableDay(Day.WEDNESDAY, 3),
                createTimetableDay(Day.THURSDAY, 4),
                createTimetableDay(Day.FRIDAY, 5)
            ),
            Timetable.Info(
                "C01", "Test Timetable", Semester(LocalDate.now(), 1)
            )
        )

        val expectedCount = (1..5).sum()
        assertEquals(expectedCount, timetable.getClassesCount())
    }

    /**
     * This test will check if the equals method of a timetable properly detects a difference
     * between two timetables where all lecturers are the same but some have different times.
     */
    @Test
    fun testDifferentTimes() {
        val parsedDir = File("src/test/resources/sampleTimetables/parsed/")
        val timetable1File = File(parsedDir, "#SPLUS4F80E0.json")
        val timetable2File = File(parsedDir, "#SPLUS4F80E0-MOD_TIME.json")
        val timetable1 = timetableHandler.getJsonTimetable(timetable1File)
        val timetable2 = timetableHandler.getJsonTimetable(timetable2File)

        if (timetable1 == null || timetable2 == null) {
            fail("No timetable resources. Check file paths.")
            return
        }

        assertEquals(timetable1, timetable2)
    }

    /**
     * This test will check if the equals method of a timetable properly detects a difference
     * between two timetables where all lecturers are the same but some have different rooms.
     */
    @Test
    fun testDifferentRooms() {
        val parsedDir = File("src/test/resources/sampleTimetables/parsed/")
        val timetable1File = File(parsedDir, "#SPLUS4F80E0.json")
        val timetable2File = File(parsedDir, "#SPLUS4F80E0-MOD_ROOM.json")
        val timetable1 = timetableHandler.getJsonTimetable(timetable1File)
        val timetable2 = timetableHandler.getJsonTimetable(timetable2File)

        if (timetable1 == null || timetable2 == null) {
            fail("No timetable resources. Check file paths.")
            return
        }

        assertEquals(timetable1, timetable2)
    }

    private fun createTimetableDay(day: Day, itemsInDay: Int): TimetableDay {
        return TimetableDay(day, createTimetableItems(itemsInDay))
    }

    private fun createTimetableItems(size: Int): ArrayList<TimetableClass> {
        val classes = mutableListOf<TimetableClass>()
        for (i in 1..size) {
            classes.add(createTimetableItem())
        }
        return ArrayList(classes)
    }

    private fun createTimetableItem(): TimetableClass {
        return createTimetableItem(
            WeeksBuilder().setRange(1, 1).getWeeks()
        )
    }

    private fun createTimetableItem(weeks: Weeks): TimetableClass {
        return TimetableClass(
            "code", "name", "room", "lecturer", TimetableClass.Type("type"),
            LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, weeks
        )
    }
}