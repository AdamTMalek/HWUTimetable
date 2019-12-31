package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*


class SemesterBuilder {
    var startDate: LocalDate = LocalDate.now()
    var value: Int = 1

    fun setFromString(string: String): SemesterBuilder {
        val formatter = DateTimeFormat.forPattern("dd MMM YYYY").withLocale(Locale.ENGLISH)
        startDate = LocalDate.parse(string, formatter)
        return this
    }

    fun setFromLocalDate(localDate: LocalDate): SemesterBuilder {
        this.startDate = localDate
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun setSemesterValue(value: Int): SemesterBuilder {
        if (!isValidSemesterValue(value))
            throw IllegalArgumentException("Semester value must be either 1 or 0. (Passed: $value)")

        this.value = value
        return this
    }

    private fun isValidSemesterValue(value: Int): Boolean {
        return value in (1..2)
    }

    fun getSemester(): Semester {
        return Semester(startDate, value)
    }
}