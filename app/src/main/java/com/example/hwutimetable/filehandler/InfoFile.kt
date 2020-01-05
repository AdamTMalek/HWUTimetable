package com.example.hwutimetable.filehandler

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File

/**
 * This class handles the tt_dict.json file responsible
 * for storing information ([TimetableInfo]) about stored documents/timetables.
 */
class InfoFile(private val directory: File) : ListFileHandler<TimetableInfo> {

    companion object {
        const val FILENAME = "tt_dict.json"
    }

    /**
     * Adds the new information to the info file
     */
    override fun save(member: TimetableInfo) {
        val file = getFile()

        // Get the list we have so far
        val infoList = mutableListOf<TimetableInfo>()
        if (file.isFile) {
            infoList.addAll(getList())
        }

        if (infoList.contains(member))
            return  // Info for this list already exists in the file

        infoList.add(member)
        return saveAll(infoList)
    }

    /**
     * Save the given [list] to the info file.
     * WARNING - This will override the existing file.
     */
    override fun saveAll(list: List<TimetableInfo>) {
        val file = File(directory, FILENAME)

        val json = Json(JsonConfiguration.Stable)
        val jsonData = json.stringify(TimetableInfo.serializer().list, list)
        file.writeText(jsonData)
    }

    /**
     * Gets the list of [TimetableInfo] stored in the info file
     */
    override fun getList(): List<TimetableInfo> {
        val file = getFile()

        if (!file.isFile)
            return emptyList()

        val string = file.readText()
        val json = Json(JsonConfiguration.Stable)
        return json.parse(TimetableInfo.serializer().list, string)
    }

    /**
     * Delete a [TimetableInfo] element from the info file
     */
    @Throws(InfoNotFoundException::class)
    override fun delete(member: TimetableInfo) {
        val list = getList().toMutableList()

        if (!list.remove(member))
            throw InfoNotFoundException(member)

        saveAll(list)
    }

    /**
     * Delete the whole info file
     */
    override fun deleteAll() {
        getFile().delete()
    }

    fun invalidateInfoFile(codes: Iterable<String>): List<TimetableInfo> {
        val infoList = getList()
        val noTimetables = infoList.filterNot { codes.contains(it.code) }
        noTimetables.forEach {
            delete(it)
        }
        return getList()  // Return updated list
    }

    /**
     * Given the timetable [name] return the [TimetableInfo] if it exists
     */
    fun getInfoByName(name: String): TimetableInfo? {
        return getList().find { it.name == name }
    }

    /**
     * Given the timetable [code] return the [TimetableInfo] if it exists
     */
    fun getInfoByCode(code: String): TimetableInfo? {
        return getList().find { it.code == code }
    }

    /**
     * Get the info file
     */
    private fun getFile() = File(directory, FILENAME)
}