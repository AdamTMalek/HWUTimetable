package com.example.hwutimetable.parser

import com.example.hwutimetable.parser.exceptions.ParserException
import org.jsoup.nodes.Document
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File

class ParserTest {
    private val document: Document = org.jsoup.Jsoup.parse(
        File("src/test/sampleTimetables/tt1.html"), "UTF-8"
    )
    private val parser: Parser = Parser(document)

    private var timetable: Timetable? = null

    @Before
    fun runParser() {
        timetable = parser.parse()
    }

    /**
     * Test if the ParserException is throw, when the Parser has a document without a Jsoup parser
     */
    @Test(expected = ParserException::class)
    fun parseNoDocParser() {
        Parser(Document("src/test/sampleTimetables/tt1.html")).parse()
    }

    @Test
    fun testNumberOfItems() {
        // There are 15 timetable items in tt1.html
        assertEquals(15, timetable!!.getTotalItems())
    }

    @Test
    fun testNumberOfItemsPerDay() {
        val expectedList = arrayOf(1, 3, 3, 4, 4)

        expectedList.forEachIndexed { index, expected ->
            assertEquals(expected, timetable!!.days[index].items.size)
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

        timetable!!.days.forEachIndexed {index, day ->
            val codes = timetable!!.days[index].items.map { it.code }
            assertTrue(expected[index].containsAll(codes))
            assertEquals(expected[index].size, timetable!!.days[index].items.size)
        }
    }

    /**
     * Tests if the document hash is the same as parsed timetable hash
     * (i.e. does the timetable get the hash of the document)
     */
    @Test
    fun testHash() {
        val docHash = Hash.get(document)
        val timetableHash = timetable!!.hash
        assertTrue(Hash.compare(docHash, timetableHash))
    }
}