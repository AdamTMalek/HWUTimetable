package com.example.hwutimetable.scraper

import org.junit.Test
import org.junit.Assert.*

import org.joda.time.LocalTime

class TimetableElementTest {
    @Test
    fun testTimes() {
        val start = LocalTime(9, 15)
        val end = LocalTime(10, 15)
        val expectedDuration = LocalTime(1, 0)

        val map = mapOf(
            "code" to "XXX",
            "name" to "XXX",
            "room" to "XXX",
            "lecturer" to "XXX",
            "type" to TimetableElementType.LEC,
            "start" to start,
            "end" to end
        )
        val lecture = TimetableElement(map)

        assertEquals("Start time must be 9:15", start, lecture.start)
        assertEquals("End time must be 9:15", end, lecture.end)
        assertEquals("The duration must be 1:00", expectedDuration, lecture.duration)
    }
}