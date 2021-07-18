package com.github.hwutimetable.parser

import com.github.hwutimetable.SampleTimetableHandler
import com.github.hwutimetable.parser.exceptions.ParserException
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class ProgrammeTimetableParserTest {
    private val classLoader = javaClass.classLoader!!
    private val backgroundCss = classLoader.getResource("activitytype.css")
    private val typeBackgroundProvider = TimetableClass.Type.OnlineBackgroundProvider(backgroundCss)
    private val document: Document = SampleTimetableHandler(typeBackgroundProvider).getDocument(
        File("src/test/resources/sampleTimetables/test-timetable-org.html")
    )

    private val parser: ProgrammeTimetableParser = ProgrammeTimetableParser(document, typeBackgroundProvider)

    private lateinit var days: Array<TimetableDay>

    @Before
    fun runParser() {
        days = parser.getTimetable()
    }

    /**
     * Test if the ParserException is throw, when the Parser has a document without a Jsoup parser
     */
    @Test(expected = ParserException::class)
    fun parseNoDocParser() {
        ProgrammeTimetableParser(
            Document("src/test/sampleTimetables/test-timetable-org.html"),
            typeBackgroundProvider
        ).getTimetable()
    }

    @Test
    fun testNumberOfItems() {
        // There are 15 timetable timetableClasses in tt1.html
        assertEquals(15, days.sumOf { day -> day.classes.size })
    }

    @Test
    fun testNumberOfItemsPerDay() {
        val expectedList = arrayOf(1, 3, 3, 4, 4)

        expectedList.forEachIndexed { index, expected ->
            assertEquals(expected, days[index].classes.size)
        }
    }

    @Test
    fun testItemsByCodes() {
        val expected = arrayOf(
            listOf("B39SA-S1"),  // Monday
            listOf("B39SA-S1", "B39AX-S1", "B39SA-S1"),  // Tuesday
            listOf("B39AX-S1", "B39AX-S1", "F29AI-S1"),  // Wednesday
            listOf("F29AI-S1", "B39AX-S1", "F29SO-S1", "F29SO-S1"),  // Thursday
            listOf("F29SO-S1", "F29AI-S1", "F29SO-S1", "F29SO-S1")  // Friday
        )

        days.forEachIndexed { index, _ ->
            val codes = days[index].classes.map { it.code }
            assertTrue(expected[index].containsAll(codes))
            assertEquals(expected[index].size, days[index].classes.size)
        }
    }

    @Test
    fun testSemester() {
        val expectedStartDate = LocalDate(2019, 9, 16)
        val actualStartDate = parser.getSemesterStartDate()

        assertEquals(expectedStartDate, actualStartDate)
    }

    @Test
    fun testStartTime() {
        fun getSampleTimetable(name: String): File {
            return File(classLoader.getResource("sampleTimetables/$name").toURI())
        }

        fun getTimetableDays(file: File): Array<TimetableDay> {
            if (!file.exists())
                throw FileNotFoundException("File $file was not found")
            val doc = Jsoup.parse(file, "UTF-8")
            return ProgrammeTimetableParser(doc, typeBackgroundProvider).getTimetable()
        }

        // Old start times (9:15)
        val oldTimetable = getTimetableDays(getSampleTimetable("test-timetable-org.html"))
        // There is a Signals lecture at 9:15 on Tuesday
        val signalsClass = oldTimetable[1].classes.first()
        assertEquals(LocalTime.parse("9:15"), signalsClass.start)

        // New start times (9:00)
        val newTimetable = getTimetableDays(getSampleTimetable("new-times-timetable.html"))
        // There is a Elect. Energy systems lecture at 9:00 on Monday
        val systemsClass = newTimetable[0].classes.first()
        assertEquals(LocalTime.parse("9:00"), systemsClass.start)
    }
}