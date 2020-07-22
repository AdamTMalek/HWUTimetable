package com.github.hwutimetable

import org.joda.time.LocalDate
import javax.inject.Inject

class CurrentLocalDateProvider @Inject constructor() : CurrentDateProvider {
    override fun getCurrentDate() = LocalDate.now()!!
}