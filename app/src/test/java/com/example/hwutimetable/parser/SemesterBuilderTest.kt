package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SemesterBuilderTest {
    @Test
    fun testBuildFromString() {
        val semester = SemesterBuilder()
            .setFromString("13 Jan 2020")
            .getSemester()

        val expected = LocalDate(2020, 1, 13)
        assertEquals(expected, semester.startDate)
    }

    @Test
    fun testBuildFromDate() {
        val expected = LocalDate(2020, 1, 13)
        val semester = SemesterBuilder()
            .setFromLocalDate(expected)
            .getSemester()

        assertEquals(expected, semester.startDate)
    }

    @Test
    fun testBuildSemesterValue() {
        val date = LocalDate(2020, 1, 13)
        val expectedSemesterValue = 1

        val semester = SemesterBuilder()
            .setFromLocalDate(date)
            .setSemesterValue(expectedSemesterValue)

        assertEquals(expectedSemesterValue, semester.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuildSemesterIllegalNegValue() {
        val date = LocalDate(2020, 1, 13)
        val expectedSemesterValue = 3

        SemesterBuilder()
            .setFromLocalDate(date)
            .setSemesterValue(expectedSemesterValue)
    }
}