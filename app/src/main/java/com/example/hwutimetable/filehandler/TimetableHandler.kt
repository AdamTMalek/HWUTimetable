package com.example.hwutimetable.filehandler

import com.example.hwutimetable.parser.Timetable

interface TimetableHandler {
    /**
     * Saves the given timetable
     * @param timetableInfo: Information of the timetable (code and name)
     */
    fun save(timetable: Timetable, timetableInfo: TimetableInfo)

    /**
     * Gets the list of the timetables stored on the device
     */
    fun getStoredTimetables(): List<TimetableInfo>

    /**
     * Gets the timetable by its information (code and name)
     */
    fun getTimetable(timetableInfo: TimetableInfo): Timetable

    /**
     * Deletes a timetable from the device
     * @return true on success, false otherwise
     */
    fun deleteTimetable(timetableInfo: TimetableInfo)

    /**
     * Deletes all timetables stored on the device
     * @return List of the deleted timetables
     */
    fun deleteAllTimetables(): List<TimetableInfo>

    /**
     * This may be run by the objects handling FileNotFound exceptions
     * thrown when timetables do not exist, but info files do.
     */
    fun invalidateList(): List<TimetableInfo>
}