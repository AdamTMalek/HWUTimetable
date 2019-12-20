package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableInfo

/**
 * Any class that checks and updates the stored timetables must implement the following interface
 */
interface UpdateHandler {
    /**
     * Using the passed collection of updatable timetables, update these timetables by
     * scraping them and saving them to the device
     * @return Collections of timetables that were updated
     */
    fun update(): Collection<TimetableInfo>

    /**
     * After performing the update, this method should notify the user that the timetables
     * were updated.
     * @param updated: Collection of updated timetables
     */
    fun notifyPostUpdate(updated: Collection<TimetableInfo>)
}