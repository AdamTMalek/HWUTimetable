package com.example.hwutimetable.filehandler

import kotlinx.serialization.Serializable

/**
 * TimetableInfo represents information about a timetable
 * that gets saved to the device.
 * @param code: Code of the timetable (from the option value from the Timetables website)
 * @param name: Readable name
 */
@Serializable
data class TimetableInfo(val code: String, val name: String, val semester: Int)