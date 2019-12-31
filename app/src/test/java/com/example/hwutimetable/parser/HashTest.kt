package com.example.hwutimetable.parser

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HashTest {

    @Test
    fun testSameFile() {
        val doc = org.jsoup.Jsoup.parse(File("src/test/resources/sampleTimetables/tt1.html"), "UTF-8")
        assertTrue("The same hash", Hash.compare(doc, doc))
    }
}