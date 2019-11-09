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
    fun testNumberOfLectures() {
        // There are 15 timetable items in tt1.html
        assertEquals(15, parser.timetableItems.size)
    }
}