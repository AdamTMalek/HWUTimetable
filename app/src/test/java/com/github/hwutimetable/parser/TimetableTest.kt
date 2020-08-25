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

    @Test
    fun testFromTimetables() {
        val linearControlDoc = timetableHandler.getDocument(
            File(resourcesDir, "sampleTimetables/linear_control.html")
        )
        val courseParser = CourseTimetableParser("B30EJ-S1", "Linear Control", linearControlDoc, typeBackgroundProvider)
        val linearControlTimetable = courseParser.getTimetable()

        val projectDoc = timetableHandler.getDocument(
            File(resourcesDir, "sampleTimetables/project.html")
        )!!
        courseParser.apply {
            courseCode = "B30UB-S1"
            courseName = "4th Year Project 1"
            setDocument(projectDoc)
        }
        val projectTimetable = courseParser.getTimetable()

        val linearControlCode = "B30EJ-S1"
        val linearControlName = "Linear Control"

        val projectCode = "B30UB-S1"
        val projectName = "4th Year Project 1"

        val timetableDays = arrayOf(
            TimetableDay(
                Day.MONDAY, arrayListOf(
                    TimetableClass(
                        linearControlCode,
                        linearControlName,
                        "MBG44",
                        "",
                        TimetableClass.Type("Workshop", whiteColor),
                        LocalTime.parse("13:15"),
                        LocalTime.parse("16:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    )
                )
            ),
            TimetableDay(
                Day.TUESDAY, arrayListOf(
                    TimetableClass(
                        linearControlCode,
                        linearControlName,
                        "LT2",
                        "Dr M. Dunnigan",
                        TimetableClass.Type("All Students", whiteColor),
                        LocalTime.parse("12:15"),
                        LocalTime.parse("13:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    ),
                )
            ),
            TimetableDay(Day.WEDNESDAY, arrayListOf()),
            TimetableDay(
                Day.THURSDAY, arrayListOf(
                    TimetableClass(
                        linearControlCode,
                        linearControlName,
                        "Online-Live",
                        "Dr M. Dunnigan",
                        TimetableClass.Type("All Students", whiteColor),
                        LocalTime.parse("12:15"),
                        LocalTime.parse("13:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    ),
                )
            ),
            TimetableDay(
                Day.FRIDAY, arrayListOf(
                    TimetableClass(
                        projectCode,
                        projectName,
                        "JW1",
                        "Dr J. Hong",
                        TimetableClass.Type("All Students", whiteColor),
                        LocalTime.parse("10:15"),
                        LocalTime.parse("11:15"),
                        WeeksBuilder().setRange(1, 12).getWeeks()
                    ),
                )
            )
        )

        val timetableInfo = Timetable.Info("X", "X", Semester(LocalDate.now(), 1), true)
        val expectedTimetable = Timetable(timetableDays, timetableInfo)
        val actualTimetable = Timetable.fromTimetables(timetableInfo, listOf(linearControlTimetable, projectTimetable))

        assertEquals(expectedTimetable, actualTimetable)
    }

    @Test
    fun testReplaceClassesOfCourse() {
        val signalsCode = "B39SA-S1"

        fun getSignalsClass(from: LocalTime, to: LocalTime) = TimetableClass(
            signalsCode,
            "Signals and Systems",
            "EM336",
            "Prof. Y Wiaux",
            TimetableClass.Type("Lec", whiteColor),
            from,
            to,
            WeeksBuilder().setRange(1, 12).getWeeks()
        )

        val timetable = timetableHandler.getHtmlTimetable(
            File(resourcesDir, "sampleTimetables/tt1.html"),
            Timetable.Info("X", "X", Semester(LocalDate.now(), 1), false)
        )
        val tuesdayClass = getSignalsClass(LocalTime.parse("10:15"), LocalTime.parse("11:15"))
        val signalsClasses = arrayOf(
            TimetableDay(Day.MONDAY, arrayListOf()),
            TimetableDay(Day.TUESDAY, arrayListOf(tuesdayClass)),
            TimetableDay(Day.WEDNESDAY, arrayListOf()),
            TimetableDay(Day.THURSDAY, arrayListOf()),
            TimetableDay(Day.FRIDAY, arrayListOf()),
        )

        timetable.replaceClassesOfCourse(signalsCode, signalsClasses)
        val actualSignalsClasses = timetable.getClassesOfCourse(signalsCode).flatMap { it.classes }

        assertEquals(1, actualSignalsClasses.size)
        assertEquals(tuesdayClass, actualSignalsClasses.first())
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