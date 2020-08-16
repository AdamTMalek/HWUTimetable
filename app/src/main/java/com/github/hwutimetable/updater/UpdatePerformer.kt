package com.github.hwutimetable.updater

import com.github.hwutimetable.parser.Timetable

/**
 * Any class that checks and updates the stored timetables must implement the following interface
 */
interface UpdatePerformer {
    /**
     * Update all timetables stored on the device. This method will not return anything,
     * if you need to get a collection of updated timetables, implement [OnUpdateFinishedListener]
     * in your class and use [addFinishedListener] to add the object as a notification receiver.
     */
    fun update()

    /**
     * Add notification receiver that will get the notification when update process
     * has started
     */
    fun addInProgressListener(receiver: OnUpdateInProgressListener)

    /**
     * Add notification receiver that will get the notification when (and what) timetables
     * were updated after running the [update()] method.
     */
    fun addFinishedListener(receiver: OnUpdateFinishedListener)

    /**
     * Notifies all registered [OnUpdateInProgressListener] that the update process has started
     */
    fun notifyUpdateInProgress()

    /**
     * After performing the update, this method will be used to notify any registered [OnUpdateFinishedListener].
     * @param updated: Collection of updated timetables
     */
    fun notifyUpdateFinished(updated: Collection<Timetable.Info>)
}