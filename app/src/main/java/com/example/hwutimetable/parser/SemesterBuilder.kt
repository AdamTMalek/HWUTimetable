package com.example.hwutimetable.parser

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.*


class SemesterBuilder {
    var startDate: LocalDate = LocalDate.now()

    fun setFromString(string: String): SemesterBuilder {
        val formatter = DateTimeFormat.forPattern("dd MMM YYYY").withLocale(Locale.ENGLISH)
        startDate = LocalDate.parse(string, formatter)
        return this
    }

    fun setFromLocalDate(localDate: LocalDate): SemesterBuilder {
        this.startDate = localDate
        return this
    }

    fun getSemester(): Semester {
        return Semester(startDate)
    }
}