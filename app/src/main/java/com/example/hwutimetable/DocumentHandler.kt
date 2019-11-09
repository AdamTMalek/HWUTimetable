package com.example.hwutimetable

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import kotlinx.serialization.*

class DocumentHandler {
    companion object {
        fun save(context: Context, document: Document, timetableInfo: TimetableInfo) {
            saveTimetableInfo(context, timetableInfo)
            saveTimetable(context, document, timetableInfo.code)
        }

        private fun saveTimetableInfo(context: Context, timetableInfo: TimetableInfo) {
            val file = File(context.filesDir, "tt_dict.json")
            val list = mutableListOf<TimetableInfo>()
            if (file.isFile) {
                list.addAll(getExistingInfo(file))
            }
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