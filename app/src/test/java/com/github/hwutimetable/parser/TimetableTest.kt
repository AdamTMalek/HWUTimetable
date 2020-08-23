package com.github.hwutimetable.parser

import com.github.hwutimetable.SampleTimetableHandler
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class TimetableTest {
    private val resourcesDir = File("src/test/resources")
    private val parsedTimetablesDirectory = File(resourcesDir, "sampleTimetables/parsed/")
    private val whiteColor = "#FFFFFF"
    private val typeBackgroundProvider = object : TimetableClass.Type.BackgroundProvider {
        override suspend fun getBackgroundColor(type: String) = whiteColor
    }
    private val timetableHandler = SampleTimetableHandler(typeBackgroundProvider)

    @Test
    fun testEmptyTimetable() {
        val timetable = Timetable(
            arrayOf(),
            Timetable.Info(
                "C01", "Test Timetable", Semester(LocalDate.now(), 1), false
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
                "C01", "Test Timetable", Semester(LocalDate.now(), 1), false
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
        val timetable1File = File(parsedTimetablesDirectory, "#SPLUS4F80E0.json")
        val timetable2File = File(parsedTimetablesDirectory, "#SPLUS4F80E0-MOD_TIME.json")
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
        val timetable1File = File(parsedTimetablesDirectory, "#SPLUS4F80E0.json")
        val timetable2File = File(parsedTimetablesDirectory, "#SPLUS4F80E0-MOD_ROOM.json")
        val timetable1 = timetableHandler.getJsonTimetable(timetable1File)
        val timetable2 = timetableHandler.getJsonTimetable(timetable2File)

        if (timetable1 == null || timetable2 == null) {
            fail("No timetable resources. Check file paths.")
            return
        }

        assertEquals(timetable1, timetable2)
    }

    @Test
    fun testGetCourse() {
        val timetableFile = File(parsedTimetablesDirectory, "../tt1.html")
        val timetable = timetableHandler.getHtmlTimetable(
            timetableFile, Timetable.Info(
                "xxx", "xxx", Semester(LocalDate.now(), 1), false
            )
        )

        val expectedListOfCodes = listOf(
            Pair("B39SA-S1", "Signals and Systems"),
            Pair("B39AX-S1", "Engineering maths and stats"),
            Pair("F29AI-S1", "Artificial Intell&Intell Agent"),
            Pair("F29SO-S1", "Software Engineering")
        )
        val actualListOfCodes = timetable.getCourses()

        assertEquals(expectedListOfCodes, actualListOfCodes)
    }

    @Test
    fun testGetClassesOfCourse() {
        val timetableFile = File(parsedTimetablesDirectory, "../tt1.html")
        val timetable = timetableHandler.getHtmlTimetable(
            timetableFile, Timetable.Info(
                "xxx", "xxx", Semester(LocalDate.now(), 1), false
            )
        )

        val expectedClasses = arrayOf(
            TimetableDay(
                Day.MONDAY, arrayListOf(
                    TimetableClass(
                        "B39SA-S1",
                        "Signals and Systems",
                        "EM336",
                        "Prof. Y Wiaux",
                        TimetableClass.Type("Lec", whiteColor),
                        LocalTime.parse("14:15"),
                        LocalTime.parse("16:15"),
                        WeeksBuilder().setRange(1, 12).getWeeks()
                    )
                )
            ),
            TimetableDay(
                Day.TUESDAY, arrayListOf(
                    TimetableClass(
                        "B39SA-S1",
                        "Signals and Systems",
                        "GR1DLb",
                        "Prof. Y Wiaux",
                        TimetableClass.Type("WKP", whiteColor),
                        LocalTime.parse("9:15"),
                        LocalTime.parse("12:15"),
                        WeeksBuilder().setRange(1, 12).getWeeks()
                    ),
                    TimetableClass(
                        "B39SA-S1",
                        "Signals and Systems",
                        "EM336",
                        "Prof. Y Wiaux",
                        TimetableClass.Type("Lec", whiteColor),
                        LocalTime.parse("16:15"),
                        LocalTime.parse("17:45"),
                        WeeksBuilder().setRange(1, 12).getWeeks()
                    )
                )
            ),
            TimetableDay(Day.WEDNESDAY, arrayListOf()),
            TimetableDay(Day.THURSDAY, arrayListOf()),
            TimetableDay(Day.FRIDAY, arrayListOf())
        )

        val actualClasses = timetable.getClassesOfCourse("B39SA-S1")
        assertTrue(expectedClasses.contentEquals(actualClasses))
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
            "code", "name", "room", "lecturer", TimetableClass.Type("type", "#222287"),
            LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, weeks
        )
    }
}