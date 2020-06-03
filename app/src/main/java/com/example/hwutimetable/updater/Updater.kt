package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableFileHandler
import com.example.hwutimetable.filehandler.TimetableInfo
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.parser.TimetableParser
import com.example.hwutimetable.scraper.TimetableScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * This class performs updating of the timetables that are stored on the device.
 */
class Updater(
    filesDir: File,
    private val parser: TimetableParser,
    private val scraper: TimetableScraper
) : UpdatePerformer {
    private val fileHandler = TimetableFileHandler(filesDir)
    private val notificationReceivers = mutableListOf<UpdateNotificationReceiver>()

    /**
     * Update all timetables stored on the device. This method will not return anything,
     * if you need to get a collection of updated timetables, implement [UpdateNotificationReceiver]
     * in your class and use [addNotificationReceiver] to add the object as a notification receiver.
     */
    override fun update() {
        notifyUpdateInProgress()

        // Launch the coroutine and "forget" about it, in that way we can start the update process
        // in the background (if it runs on the UI thread) and go back to rendering the UI. After
        // the update process is finished, the method will notify the registered notification
        // about the result.
        GlobalScope.launch {
            scraper.setup()

            val timetables = getStoredTimetables()
            val updated = mutableListOf<TimetableInfo>()
            timetables.forEach { timetable ->
                val newTimetable = getTimetable(timetable)

                if (isUpdated(timetable, newTimetable)) {
                    saveTimetable(newTimetable, timetable)
                    updated.add(timetable)
                }
            }

            withContext(Dispatchers.Main) {
                notifyUpdateFinished(updated)
            }
        }
    }

    private suspend fun getTimetable(info: TimetableInfo): Timetable {
        val doc = scraper.getTimetable(info.code, info.semester)
        return parser.setDocument(doc).getTimetable()
    }

    /**
     * Check if the timetable has been updated. This loads the stored timetable (using the [storedInfo]
     * and compares the two timetables.
     * @param storedInfo: [TimetableInfo] of the stored timetable
     * @param newTimetable: Scraped timetable that may or may not have been updated
     * @return true if the [newTimetable] is different from the one stored on the device.
     */
    private fun isUpdated(storedInfo: TimetableInfo, newTimetable: Timetable): Boolean {
        val oldTimetable = fileHandler.getTimetable(storedInfo)
        return oldTimetable != newTimetable
    }

    /**
     * Save the timetable on the device
     */
    private fun saveTimetable(timetable: Timetable, info: TimetableInfo) {
        fileHandler.save(timetable, info)
    }

    /**
     * Get the collection of stored timetables (as [TimetableInfo]
     */
    private fun getStoredTimetables(): Collection<TimetableInfo> {
        return fileHandler.getStoredTimetables()
    }

    /**
     * Add notification receiver that will get the notification when (and what) timetables
     * were updated after running the [update()] method.
     */
    override fun addNotificationReceiver(receiver: UpdateNotificationReceiver) {
        notificationReceivers.add(receiver)
    }

    /**
     * Inform all registered [UpdateNotificationReceiver] receivers that the update process has started
     */
    override fun notifyUpdateInProgress() {
        notificationReceivers.forEach { it.onUpdateInProgress() }
    }

    /**
     * After performing the update, this method will be used to notify any registered [UpdateNotificationReceiver].
     * @param updated: Collection of updated timetables
     */
    override fun notifyUpdateFinished(updated: Collection<TimetableInfo>) {
        notificationReceivers.forEach { it.onUpdateFinished(updated) }
    }
}