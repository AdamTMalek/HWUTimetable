package com.example.hwutimetable.parser

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class HashTest {

    @Test
    fun testSameFile() {
        val doc = org.jsoup.Jsoup.parse(File("src/test/sampleTimetables/tt1.html"), "UTF-8")
        assertTrue("The same hash", Hash.compare(doc, doc))
    }
}