package com.example.hwutimetable.filehandler

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File

/**
 * This object/singleton handles the tt_dict.json file responsible
 * for storing information ([TimetableInfo]) about stored documents/timetables.
 */
object InfoFile : ListFileHandler<TimetableInfo> {
    private const val FILENAME = "tt_dict.json"

    /**
     * Adds the new information to the info file
     */
    override fun save(context: Context, member: TimetableInfo) {
        val file = getFile(context)

        // Get the list we have so far
        val infoList = mutableListOf<TimetableInfo>()
        if (file.isFile) {
            infoList.addAll(getList(context))
        }

        if (infoList.contains(member))
            return  // Info for this list already exists in the file

        infoList.add(member)
        return saveAll(context, infoList)
    }

    /**
     * Save the given [list] to the info file.
     * WARNING - This will override the existing file.
     */
    override fun saveAll(context: Context, list: List<TimetableInfo>) {
        val file = File(context.filesDir, FILENAME)

        val json = Json(JsonConfiguration.Stable)
        val jsonData = json.stringify(TimetableInfo.serializer().list, list)
        file.writeText(jsonData)
    }

    /**
     * Gets the list of [TimetableInfo] stored in the info file
     */
    override fun getList(context: Context): List<TimetableInfo> {
        val file = getFile(context)

        if (!file.isFile)
            return emptyList()

        val string = file.readText()
        val json = Json(JsonConfiguration.Stable)
        return json.parse(TimetableInfo.serializer().list, string)
    }

    /**
     * Delete a [TimetableInfo] element from the info file
     */
    override fun delete(context: Context, member: TimetableInfo) {
        val list = getList(context).toMutableList()

        if (!list.remove(member))
            throw FileHandlerException(
                "The info list does not have the code ${member.code}",
                FileHandlerException.Reason.CORRUPTED
            )

        saveAll(context, list)
    }

    /**
     * Delete the whole info file
     */
    override fun deleteAll(context: Context) {
        getFile(context).delete()
    }

    /**
     * Given the timetable [name] return the [TimetableInfo] if it exists
     */
    fun getInfoByName(context: Context, name: String): TimetableInfo? {
        return getList(context).find { it.name == name }
    }

    /**
     * Given the timetable [code] return the [TimetableInfo] if it exists
     */
    fun getInfoByCode(context: Context, code: String): TimetableInfo? {
        return getList(context).find { it.code == code }
    }

    /**
     * Get the info file
     */
    private fun getFile(context: Context) = File(context.filesDir, FILENAME)
}