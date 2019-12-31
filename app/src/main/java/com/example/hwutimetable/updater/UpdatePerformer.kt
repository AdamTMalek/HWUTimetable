package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableInfo

/**
 * Any class that checks and updates the stored timetables must implement the following interface
 */
interface UpdatePerformer {
    /**
     * Update all timetables stored on the device. This method will not return anything,
     * if you need to get a collection of updated timetables, implement [UpdateNotificationReceiver]
     * in your class and use [addNotificationReceiver] to add the object as a notification receiver.
     */
    fun update()

    /**
     * Add notification receiver that will get the notification when (and what) timetables
     * were updated after running the [update()] method.
     */
    fun addNotificationReceiver(receiver: UpdateNotificationReceiver)

    /**
     * Notifies all registered [UpdateNotificationReceiver] receivers that the update process has started
     */
    fun notifyUpdateInProgress()

    /**
     * Notifies all registered [UpdateNotificationReceiver] receivers that the update process has finished
     */
    fun notifyUpdateFinished()

    /**
     * After performing the update, this method will be used to notify any registered [UpdateNotificationReceiver].
     * @param updated: Collection of updated timetables
     */
    fun notifyUpdateFinished(updated: Collection<TimetableInfo>)
}