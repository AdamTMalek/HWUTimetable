package com.example.hwutimetable.filehandler

import com.example.hwutimetable.parser.*
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class TimetableFileHandlerTest {
    companion object {
        private val dir_path: String = System.getProperty("user.dir") + "/tfh_test_dir"
        private val test_dir = File(dir_path)
        private lateinit var fileHandler: TimetableFileHandler

        @BeforeClass
        @JvmStatic
        fun setup() {
            if (test_dir.exists()) {
                test_dir.delete()
            }

            val success = test_dir.mkdir()
            if (!success) {
                fail("Unable to create timetable file handler test directory")
                return
            }
            fileHandler = TimetableFileHandler(test_dir)
        }

        @AfterClass
        @JvmStatic
        fun cleanUp() {
            if (!test_dir.exists())
                fail("The timetable file handler test directory does not exist before cleaning")
            test_dir.delete()
        }
    }

    @After
    fun deleteAllStoredTimetables() {
        val files = test_dir.listFiles()
        files?.forEach { file ->
            file.delete()
        }
    }

    @Test
    fun testSaveTimetable() {
        val info = saveTimetable()

        val file = getFileByInfo(info)
        assertTrue(file.exists())
    }

    @Test
    fun testGetStoredTimetables() {
        val infoList = mutableListOf<TimetableInfo>()

        for (i in 1..5) {
            infoList.add(saveTimetable(code = "code$i"))
        }

        val savedInfoList = fileHandler.getStoredTimetables()
        assertEquals(infoList, savedInfoList)
    }

    @Test
    fun testGetTimetable() {
        val actualTimetable = Timetable(
            generateTimetableDays(),
            Semester(LocalDate.now(), 1)
        )

        val timetableInfo = TimetableInfo("code", "name", 1)
        fileHandler.save(actualTimetable, timetableInfo)
        val savedTimetable = fileHandler.getTimetable(timetableInfo)

        assertEquals("Are timetables the same?", actualTimetable, savedTimetable)
    }

    @Test(expected = FileNotFoundException::class)
    fun testGetCorrupted() {
        val timetableInfo = TimetableInfo("xxx", "xxx", 1)
        fileHandler.getTimetable(timetableInfo)
    }

    @Test
    fun testDeleteTimetable() {
        val info = saveTimetable()
        fileHandler.deleteTimetable(info)

        val file = getFileByInfo(info)
        assertFalse("Has the file been deleted?", file.exists())
    }

    @Test(expected = InfoNotFoundException::class)
    fun testDeleteCorrupted() {
        val info = TimetableInfo("xxx", "xxx", 1)
        fileHandler.deleteTimetable(info)
    }

    @Test
    fun testInvalidateNoTimetable() {
        val infoA = saveTimetable(code = "codeA")
        val infoB = saveTimetable(code = "codeB")

        val file = getFileByInfo(infoA)
        file.delete()

        val saved = fileHandler.invalidateList()

        assertTrue("Was the infoA deleted?", !saved.contains(infoA))
        assertTrue(saved.contains(infoB))
        assertEquals(1, saved.size)
    }

    @Test
    fun testInvalidateNoInfo() {
        val info = saveTimetable()

        val infoFile = File(dir_path + "/${InfoFile.FILENAME}")
        infoFile.delete()

        val saved = fileHandler.invalidateList()
        assertTrue("Returned empty \"saved\" list?", saved.isEmpty())

        val timetableFile = getFileByInfo(info)
        assertTrue("Timetable file deleted?", !timetableFile.exists())
    }


    private fun saveTimetable(code: String = "xxx"): TimetableInfo {
        val timetable = Timetable(
            generateTimetableDays(),
            Semester(LocalDate.now(), 1)
        )
        val info = TimetableInfo(code, "xxx", 1)
        fileHandler.save(timetable, info)
        return info
    }

    private fun getFileByInfo(info: TimetableInfo) =
        File(dir_path + "/${info.code}.json")

    private fun generateTimetableDays(): Array<TimetableDay> {
        return arrayOf(
            generateTimetableDay(Day.MONDAY, 1),
            generateTimetableDay(Day.TUESDAY, 2),
            generateTimetableDay(Day.WEDNESDAY, 3)
        )
    }

    private fun generateTimetableDay(day: Day, itemsInDay: Int): TimetableDay {
        val list = mutableListOf<TimetableItem>()
        for (i in 1..itemsInDay)
            list.add(generateTimetableItem())
        return TimetableDay(day, ArrayList(list))
    }

    private fun generateTimetableItem(): TimetableItem {
        return TimetableItem(
            "code",
            "name",
            "room",
            "lecturer",
            ItemType("type"),
            LocalTime.MIDNIGHT,
            LocalTime.MIDNIGHT,
            WeeksBuilder()
                .setRange(1, 1)
                .getWeeks()
        )
    }
}