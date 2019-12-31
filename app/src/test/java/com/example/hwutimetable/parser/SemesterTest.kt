package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test


class SemesterTest {
    private val semester = Semester(LocalDate(2020, 1, 13), 1)

    @Test
    fun testGetFirstWeek() {
        val currentDate = LocalDate(2020, 1, 13)
        val week = semester.getWeek(currentDate)

        assertEquals(1, week)
    }

    @Test
    fun testGetLastWeek() {
        val currentDate = LocalDate(2020, 4, 5)
        val week = semester.getWeek(currentDate)

        assertEquals(12, week)
    }

    @Test
    fun testGetWeek5() {
        val currentDate = LocalDate(2020, 2, 12)
        val week = semester.getWeek(currentDate)

        assertEquals(5, week)
    }
}