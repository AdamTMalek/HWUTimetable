package com.example.hwutimetable.parser

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class HashTest {

    @Test
    fun testSameFile() {
        val parser = Parser(
            org.jsoup.Jsoup.parse(File("src/test/sampleTimetables/tt1.html"), "UTF-8")
        )
        val hash = parser.parse().hash
        assertTrue("The same hash", Hash.compare(hash, hash))
    }
}