package com.github.hwutimetable.changelog

import android.app.AlertDialog
import android.content.Context
import com.github.hwutimetable.BuildConfig
import com.github.hwutimetable.R
import org.xmlpull.v1.XmlPullParser


/**
 * The [ChangeLog] class is responsible for parsing the [R.xml.changelog] file
 * and showing the changes to the user via [showRecentIfAfterUpdate] method.
 *
 * @param context: Activity context
 */
class ChangeLog(private val context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(context.getString(R.string.shared_pref_file_key), Context.MODE_PRIVATE)

    private val lastRanVersionKey
        get() = context.getString(R.string.last_ran_version)

    private val lastRanVersion
        get() = sharedPreferences.getInt(lastRanVersionKey, -1)

    /**
     * Returns true if it's the first time the app is run after an update.
     */
    private val isAfterUpdate: Boolean
        get() = BuildConfig.VERSION_CODE != lastRanVersion


    /**
     * Checks if the app is after an update, and if it is it shows the recent changes
     * that are stored in [R.xml.changelog].
     */
    fun showRecentIfAfterUpdate() {
        if (isAfterUpdate) {
            val recentLogEntry = ChangeLogXmlParser(context).getRecentChanges().first()
            showRecent(recentLogEntry)
        }
    }

    private fun showRecent(logEntry: ChangeLogXmlParser.ChangeLogEntry) {
        val bullet = "\u2022"
        val changesList = logEntry.changes.joinToString(separator = "\n$bullet ", prefix = "$bullet ", postfix = "")
        val dialog = AlertDialog.Builder(context).run {
            val title = "${context.getString(R.string.changelog_dialog_title)} ${logEntry.versionName}"
            setTitle(title)
            setMessage(changesList)
            setPositiveButton(R.string.changelog_dialog_pos_button) { dialog, _ -> dialog.dismiss() }
            create()
        }
        dialog.show()
    }

    /**
     * Parser class for parsing the [R.xml.changelog]
     */
    private class ChangeLogXmlParser(private val context: Context) {
        /**
         * Class for storing [versionCode], [versionName] and the [changes].
         */
        data class ChangeLogEntry(val versionCode: Int, val versionName: String, val changes: List<String>)

        private val parser = getParser()
        private val namespace: String? = null

        private fun getParser(): XmlPullParser {
            return context.resources.getXml(R.xml.changelog).apply {
                next()
                next()
            }
        }

        /**
         * Returns the recent changes (i.e. only the ones belonging to the new version)
         */
        fun getRecentChanges(): List<ChangeLogEntry> {
            return readChangelog(onlyRecent = true)
        }

        private fun readChangelog(onlyRecent: Boolean): List<ChangeLogEntry> {
            val entries = mutableListOf<ChangeLogEntry>()

            parser.require(XmlPullParser.START_TAG, namespace, "changelog")
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG)
                    continue

                if (parser.name != "version")
                    skip()

                val versionCode = parser.getAttributeValue(null, "versionCode").toInt()

                if (onlyRecent && BuildConfig.VERSION_CODE <= versionCode) {
                    entries.add(readVersionEntry())
                    break  // We have read the changes for the newest version - we can return the result
                } else {
                    // Here, we're either reading all changes for all versions
                    // or the version tag we're currently on is not the version we're looking for
                    entries.add(readVersionEntry())
                }
            }

            return entries
        }

        private fun readVersionEntry(): ChangeLogEntry {
            parser.require(XmlPullParser.START_TAG, namespace, "version")
            val versionCode = parser.getAttributeValue(null, "versionCode").toInt()
            val versionName = parser.getAttributeValue(null, "versionName")
            val changes = mutableListOf<String>()
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG)
                    continue
                changes.add(readChange())
            }
            return ChangeLogEntry(versionCode, versionName, changes)
        }

        private fun readChange(): String {
            parser.require(XmlPullParser.START_TAG, namespace, "change")
            val change = readText()
            parser.require(XmlPullParser.END_TAG, namespace, "change")
            return change
        }

        private fun readText(): String {
            var result = ""
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.text
                parser.nextTag()
            }
            return result
        }

        private fun skip() {
            if (parser.eventType != XmlPullParser.START_TAG)
                throw IllegalStateException()

            var depth = 1
            while (depth != 0) {
                when (parser.next()) {
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_TAG -> depth--
                }
            }
        }
    }
}