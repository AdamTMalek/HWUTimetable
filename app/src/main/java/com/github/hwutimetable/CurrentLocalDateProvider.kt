package com.github.hwutimetable

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import javax.inject.Inject

class CurrentLocalDateProvider @Inject constructor() : CurrentDateProvider {
    override fun getCurrentDate() = LocalDate.now()!!
    override fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now()
}