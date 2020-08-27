package com.github.hwutimetable

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TestDateProvider @Inject constructor() : CurrentDateProvider {
    var dateTime: LocalDateTime = LocalDateTime.now()
    override fun getCurrentDate(): LocalDate {
        return dateTime.toLocalDate()
    }

    override fun getCurrentDateTime(): LocalDateTime {
        return dateTime
    }

    fun setDate(date: LocalDate) {
        dateTime = date.toLocalDateTime(LocalTime.MIDNIGHT)
    }
}
