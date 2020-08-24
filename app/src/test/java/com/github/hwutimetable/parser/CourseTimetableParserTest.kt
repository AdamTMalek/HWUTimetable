package com.github.hwutimetable.parser

import com.github.hwutimetable.SampleTimetableHandler
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.joda.time.LocalTime
import org.junit.Test
import java.io.File

class CourseTimetableParserTest {
    private val courseCode = "B30EJ-S1"
    private val courseName = "Linear Control"
    private val whiteColour = "#FFFFFF"
    private val backgroundProvider = object : TimetableClass.Type.BackgroundProvider {
        override suspend fun getBackgroundColor(type: String): String {
            return whiteColour
        }
    }
    private val timetableFile = File("src/test/resources/sampleTimetables/linear_control.html")
    private val timetableDocument = SampleTimetableHandler(backgroundProvider).getDocument(timetableFile)
    private val parser = CourseTimetableParser(courseCode, courseName, timetableDocument, backgroundProvider)

    @Test
    fun testParsesCorrectly() {
        val expectedTimetable = arrayOf(
            TimetableDay(
                Day.MONDAY,
                arrayListOf(
                    getClass(
                        "MBG44",
                        "",
                        TimetableClass.Type("Workshop", whiteColour),
                        LocalTime.parse("13:15"),
                        LocalTime.parse("16:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    )
                )
            ),
            TimetableDay(
                Day.TUESDAY,
                arrayListOf(
                    getClass(
                        "LT2",
                        "Dr M. Dunnigan",
                        TimetableClass.Type("All Students", whiteColour),
                        LocalTime.parse("12:15"),
                        LocalTime.parse("13:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    )
                )
            ),
            TimetableDay(
                Day.WEDNESDAY,
                arrayListOf()
            ),
            TimetableDay(
                Day.THURSDAY,
                arrayListOf(
                    getClass(
                        "Online-Live",
                        "Dr M. Dunnigan",
                        TimetableClass.Type("All Students", whiteColour),
                        LocalTime.parse("12:15"),
                        LocalTime.parse("13:15"),
                        WeeksBuilder().setRange(1, 11).getWeeks()
                    )
                )
            ),
            TimetableDay(
                Day.FRIDAY,
                arrayListOf()
            )
        )

        assertTrue(expectedTimetable.contentEquals(parser.getTimetable()))
    }

    @Test
    fun testNameIsSet() {
        val courseClass = parser.getTimetable().find { it.classes.isNotEmpty() }!!.classes.first()
        assertEquals(courseName, courseClass.name)
    }

    @Test
    fun testCodeIsSet() {
        val courseClass = parser.getTimetable().find { it.classes.isNotEmpty() }!!.classes.first()
        assertEquals(courseCode, courseClass.code)
    }

    private fun getClass(
        room: String,
        lecturer: String,
        type: TimetableClass.Type,
        from: LocalTime,
        to: LocalTime,
        weeks: Weeks
    ) =
        TimetableClass(courseCode, courseName, room, lecturer, type, from, to, weeks)
}