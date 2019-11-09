package com.example.hwutimetable

import android.content.Context
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException

class DocumentSaver {
    companion object {
        fun save(context: Context, document: Document, timetableName: String): Boolean {
            var success = true
            try {
                val file = File(context.filesDir, "$timetableName.html")
                file.writeText(document.outerHtml())
            } catch (exception: IOException) {
                success = false
            }

            return success
        }
    }
}