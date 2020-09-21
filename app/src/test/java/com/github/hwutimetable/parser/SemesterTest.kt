package com.github.hwutimetable.parser

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
    fun testGetFirstWeekBeforeSemesterStarts() {
        val currentDate = LocalDate(2020, 1, 1)
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
    fun testGetLastWeekAfterSemesterEnds() {
        val currentDate = LocalDate(2020, 5, 1)
        val week = semester.getWeek(currentDate)

        assertEquals(12, week)
    }

    @Test
    fun testGetWeek5() {
        val currentDate = LocalDate(2020, 2, 12)
        val week = semester.getWeek(currentDate)

        assertEquals(5, week)
    }

    /**
     * This test will check if during the weekend, the next week will be returned.
     */
    @Test
    fun testReturnsNextWeekOnSaturday() {
        val saturday = LocalDate(2020, 1, 18)
        val week = semester.getWeek(saturday)

        assertEquals(2, week)
    }

    /**
     * This test will check if during the weekend, the next week will be returned.
     */
    @Test
    fun testReturnsNextWeekOnSunday() {
        val sunday = LocalDate(2020, 1, 19)
        val week = semester.getWeek(sunday)

        assertEquals(2, week)
    }
}