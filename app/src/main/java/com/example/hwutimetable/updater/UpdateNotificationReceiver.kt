package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableInfo

/**
 * Any class that uses the [Updater] and wants to receive post-update notification must implement this interface
 */
interface UpdateNotificationReceiver {
    /**
     * This method will be invoked by a [UpdatePerformer] object that updates the stored timetables
     * The performer will send a notification to any registered [UpdateNotificationReceiver]
     * by invoking this method and passing the collection of updated timetables
     */
    fun postUpdateCallback(updatedTimetables: Collection<TimetableInfo>)
}