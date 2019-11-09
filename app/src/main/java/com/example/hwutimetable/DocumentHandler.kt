package com.example.hwutimetable

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import kotlinx.serialization.*

/**
 * This class provides methods that allow saving the JSoup documents as HTML files
 * and keeps a record of any additional information for a timetable
 */
class DocumentHandler {
    companion object {
        /**
         * Saves the given [document] (timetable) as an HTML timetable and the [timetableInfo]
         * in a JSON dictionary so that the [document] name, which is the group code,
         * can be easily mapped to a readable name of the group.
         * @param context: Context of the application - used to get the files directory
         * @param document: JSoup document of the timetable
         * @param timetableInfo: Information of the timetable (code and name)
         */
        fun save(context: Context, document: Document, timetableInfo: TimetableInfo) {
            saveTimetableInfo(context, timetableInfo)
            saveTimetable(context, document, timetableInfo.code)
        }

        private fun saveTimetableInfo(context: Context, timetableInfo: TimetableInfo) {
            val file = File(context.filesDir, "tt_dict.json")

            // First, get the list have so far
            val list = mutableListOf<TimetableInfo>()
            if (file.isFile) {
                list.addAll(getExistingInfo(file))
            }

            // Add the new info and save
            list.add(timetableInfo)
            saveInfoAsJson(file, list)
        }

        private fun getExistingInfo(file: File): List<TimetableInfo> {
            val string = file.readText()
            val json = Json(JsonConfiguration.Stable)
            return json.parse(TimetableInfo.serializer().list, string)
        }

        private fun saveInfoAsJson(file: File, list: List<TimetableInfo>) {
            val json = Json(JsonConfiguration.Stable)
            val jsonData = json.stringify(TimetableInfo.serializer().list, list)
            file.writeText(jsonData)
        }

        /**
         * Save the timetable as HTML document with [timetableName] as the name
         * (without the extension as it is added in the method)
         * @param context: Context of the application - used to get the files directory
         * @param document: Timetable to save
         * @param timetableName: Group code
         */
        private fun saveTimetable(
            context: Context,
            document: Document,
            timetableName: String
        ) {
            try {
                val file = File(context.filesDir, "$timetableName.html")
                file.writeText(document.outerHtml())
            } catch (exception: IOException) {
                Log.e("DocSaver", "IOException has occurred when writing to the file")
            }
        }
    }
}