package com.github.hwutimetable

import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class InfoListPopulator(private val fileHandler: TimetableFileHandler) {
    private fun getInfo(timetableNumber: Int): Timetable.Info {
        val semester = Semester(LocalDate.now(), 1)
        val code = "C${timetableNumber.toString().padStart(2, '0')}"
        return Timetable.Info(code, "Timetable $timetableNumber", semester, LocalTime.parse("9:00"), false)
    }

    /**
     * Populates the info list with [count] timetables. Each named Timetable [1, 2 or 3]
     */
    fun populateInfoList(count: Int = 3) {
        val timetables = sequence {
            var number = 1
            while (true) {
                yield(Timetable(emptyArray(), getInfo(number)))
                number++
            }
        }

        timetables.take(count).forEach { timetable ->
            fileHandler.save(timetable)
        }
    }
}