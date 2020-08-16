package com.github.hwutimetable.parser

import com.github.hwutimetable.parser.exceptions.ParserException
import org.joda.time.LocalDate
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class ParserTest {
    private val document: Document = org.jsoup.Jsoup.parse(
        File("src/test/resources/sampleTimetables/tt1.html"), "UTF-8"
    )
    private val parser: Parser = Parser(document)

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
        Parser(Document("src/test/sampleTimetables/tt1.html")).getTimetable()
    }

    @Test
    fun testNumberOfItems() {
        // There are 15 timetable timetableClasses in tt1.html
        assertEquals(15, days.sumBy { day -> day.classes.size })
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
}