package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableFileHandler
import com.example.hwutimetable.filehandler.TimetableInfo
import com.example.hwutimetable.parser.Parser
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.scraper.Scraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * This class performs updating of the timetables that are stored on the device.
 * Important - updating will be performed on the thread that invokes the [update] method.
 * If object invoking the [update] method runs on the UI thread, the [update] will throw the
 * [android.os.NetworkOnMainThreadException] during scraping.
 */
class Updater(filesDir: File) : UpdatePerformer {
    private val fileHandler = TimetableFileHandler(filesDir)
    private val notificationReceivers = mutableListOf<UpdateNotificationReceiver>()

    /**
     * Update all timetables stored on the device. This method will not return anything,
     * if you need to get a collection of updated timetables, implement [UpdateNotificationReceiver]
     * in your class and use [addNotificationReceiver] to add the object as a notification receiver.
     */
    override fun update() {
        // Launch the coroutine and "forget" about it, in that way we can start the update process
        // in the background (if it runs on the UI thread) and go back to rendering the UI. After
        // the update process is finished, the method will notify the registered notification
        // about the result.
        GlobalScope.launch {
            val timetables = getStoredTimetables()
            val updated = mutableListOf<TimetableInfo>()
            timetables.forEach { timetable ->
                val newTimetable = getTimetable(timetable)

                if (isUpdated(timetable, newTimetable)) {
                    saveTimetable(newTimetable, timetable)
                    updated.add(timetable)
                }
            }

            notifyPostUpdate(updated)
        }
    }

    private suspend fun getTimetable(info: TimetableInfo): Timetable {
        return withContext(Dispatchers.IO) {
            val scraper = Scraper()
            val doc = scraper.getTimetable(info.code, info.semester)
            Parser(doc).parse()
        }
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
     * After performing the update, this method will be used to notify any registered [UpdateNotificationReceiver].
     * @param updated: Collection of updated timetables
     */
    override fun notifyPostUpdate(updated: Collection<TimetableInfo>) {
        notificationReceivers.forEach { receiver ->
            receiver.postUpdateCallback(updated)
        }
    }
}