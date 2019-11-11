package com.example.hwutimetable.parser

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File

class ParserTest {
    private val parser: Parser = Parser(
        org.jsoup.Jsoup.parse(File("src/test/sampleTimetables/tt1.html"), "UTF-8")
    )

    @Before
    fun runParser() {
        parser.parse()
    }

    /**
     * Test if the ParserException is throw, when the Parser has a document without a Jsoup parser
     */
    @Test(expected = ParserException::class)
    fun parseNoDocParser() {
        Parser(org.jsoup.nodes.Document("src/test/sampleTimetables/tt1.html")).parse()
    }

    @Test
    fun testNumberOfItems() {
        // There are 15 timetable items in tt1.html
        val lectures = parser.timetableItems.sumBy { it.size }
        assertEquals(15, lectures)
    }

    @Test
    fun testNumberOfItemsPerDay() {
        val expectedList = arrayOf(1, 3, 3, 4, 4)

        expectedList.forEachIndexed { index, expected ->
            assertEquals(expected, parser.timetableItems[index].size)
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

        parser.timetableItems.forEachIndexed { index, itemsOfDay ->
            val codes = itemsOfDay.map { it.code }
            assertTrue(expected[index].containsAll(codes))
            assertEquals(expected[index].size, itemsOfDay.size)
        }
    }
}