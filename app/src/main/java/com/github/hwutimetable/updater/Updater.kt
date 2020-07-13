package com.github.hwutimetable.updater

import android.content.Context
import com.github.hwutimetable.R
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableParser
import com.github.hwutimetable.scraper.TimetableScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import java.io.File


/**
 * This class performs updating of the timetables that are stored on the device.
 */
class Updater(
    filesDir: File,
    private val parser: TimetableParser,
    private val scraper: TimetableScraper,
    private val context: Context?
) : UpdatePerformer {
    private val fileHandler = TimetableFileHandler(filesDir)
    private val inProgressListeners = mutableListOf<OnUpdateInProgressListener>()
    private val finishedListeners = mutableListOf<OnUpdateFinishedListener>()

    constructor(filesDir: File, parser: TimetableParser, scraper: TimetableScraper) : this(
        filesDir,
        parser,
        scraper,
        null
    )

    /**
     * Update all timetables stored on the device. This method will not return anything,
     * if you need to get a collection of updated timetables, implement [OnUpdateFinishedListener]
     * in your class and use [addFinishedListener] to add the object as a notification receiver.
     */
    override fun update() {
        notifyUpdateInProgress()

        // Launch the coroutine and "forget" about it, in that way we can start the update process
        // in the background (if it runs on the UI thread) and go back to rendering the UI. After
        // the update process is finished, the method will notify the registered notification
        // about the result.
        GlobalScope.launch {
            scraper.setup()

            val savedTimetablesInfoList = getStoredTimetables()
            val updated = mutableListOf<Timetable.TimetableInfo>()
            savedTimetablesInfoList.forEach { savedInfo ->
                val savedTimetable = fileHandler.getTimetable(savedInfo)
                val newTimetable = getTimetable(savedInfo)

                if (isUpdated(savedTimetable, newTimetable)) {
                    saveTimetable(newTimetable)
                    updated.add(savedInfo)
                }
            }

            saveUpdateDate()
            withContext(Dispatchers.Main) {
                notifyUpdateFinished(updated)
            }
        }
    }

    private suspend fun getTimetable(info: Timetable.TimetableInfo): Timetable {
        val doc = scraper.getTimetable(info.code, info.semester.number)
        return Timetable(parser.setDocument(doc).getTimetable(), info)
    }

    /**
     * Check if the timetable has been updated (checks for difference)
     * @param oldTimetable: The stored timetable
     * @param newTimetable: Scraped timetable that may or may not have been updated
     * @return true if the [newTimetable] is different from the one stored on the device.
     */
    private fun isUpdated(oldTimetable: Timetable, newTimetable: Timetable): Boolean {
        return oldTimetable != newTimetable
    }

    /**
     * Save the timetable on the device
     */
    private fun saveTimetable(timetable: Timetable) {
        fileHandler.save(timetable)
    }

    /**
     * Get the collection of stored timetables (as [Timetable.TimetableInfo]
     */
    private fun getStoredTimetables(): Collection<Timetable.TimetableInfo> {
        return fileHandler.getStoredTimetables()
    }

    override fun addInProgressListener(receiver: OnUpdateInProgressListener) {
        inProgressListeners.add(receiver)
    }

    override fun addFinishedListener(receiver: OnUpdateFinishedListener) {
        finishedListeners.add(receiver)
    }

    /**
     * Inform all registered [OnUpdateInProgressListener] receivers that the update process has started
     */
    override fun notifyUpdateInProgress() {
        inProgressListeners.forEach { it.onUpdateInProgress() }
    }

    /**
     * Updates (saves) the current date and time as a SharedPreference.
     * This method will be executed after the update checks have been performed.
     */
    private fun saveUpdateDate() {
        if (context == null)
            return

        val timestamp = (DateTime().millis / 1000).toInt()
        val preferencesName = context.getString(R.string.update_details)
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        with(preferences.edit()) {
            putInt(context.getString(R.string.last_update), timestamp)
            commit()
        }
    }

    /**
     * After performing the update, this method will be used to notify any registered [OnUpdateFinishedListener].
     * @param updated: Collection of updated timetables
     */
    override fun notifyUpdateFinished(updated: Collection<Timetable.TimetableInfo>) {
        finishedListeners.forEach { it.onUpdateFinished(updated) }
    }
}