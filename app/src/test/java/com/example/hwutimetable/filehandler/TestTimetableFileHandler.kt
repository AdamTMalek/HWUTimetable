package com.example.hwutimetable.filehandler

import com.example.hwutimetable.parser.*
import org.joda.time.LocalTime
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class TestTimetableFileHandler {
    companion object {
        private val dir_path: String = System.getProperty("user.dir") + "/tfh_test_dir"
        private val test_dir = File(dir_path)
        private lateinit var fileHandler: TimetableFileHandler

        @BeforeClass
        @JvmStatic
        fun setup() {
            if (test_dir.exists()) {
                fail("The timetable file handler test directory should not exist prior to testing")
                return
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
        files?.forEach {file ->
            file.delete()
        }
    }

    @Test
    fun testSaveTimetable() {
        val timetable = Timetable(
            ByteArray(0),
            generateTimetableDays()
        )

        val timetableInfo = TimetableInfo("code", "name")
        fileHandler.save(timetable, timetableInfo)

        val file = File(dir_path + "/${timetableInfo.code}.json")
        assertTrue(file.exists())
    }

    @Test
    fun testGetStoredTimetables() {
        val infoList = mutableListOf<TimetableInfo>()

        for (i in 1..5) {
            val timetable = Timetable(ByteArray(0), generateTimetableDays())
            val info = TimetableInfo("code$i", "name$i")
            infoList.add(info)
            fileHandler.save(timetable, info)
        }

        val savedInfoList = fileHandler.getStoredTimetables()
        assertEquals(infoList, savedInfoList)
    }

    @Test
    fun testGetTimetable() {
        val actualTimetable = Timetable(
            ByteArray(0),
            generateTimetableDays()
        )

        val timetableInfo = TimetableInfo("code", "name")
        fileHandler.save(actualTimetable, timetableInfo)
        val savedTimetable = fileHandler.getTimetable(timetableInfo)

        assertEquals("Are timetables the same?", actualTimetable, savedTimetable)
    }

    @Test(expected = FileNotFoundException::class)
    fun testGetCorrupted() {
        val timetableInfo = TimetableInfo("xxx", "xxx")
        fileHandler.getTimetable(timetableInfo)
    }

    @Test
    fun testDeleteTimetable() {
        val timetable = Timetable(ByteArray(0), generateTimetableDays())
        val info = TimetableInfo("xxx", "xxx")
        fileHandler.save(timetable, info)
        fileHandler.deleteTimetable(info)

        val file = File(dir_path + "/${info.code}.json")
        assertFalse("Has the file been deleted?", file.exists())
    }

    @Test(expected = InfoNotFoundException::class)
    fun testDeleteCorrupted() {
        val info = TimetableInfo("xxx", "xxx")
        fileHandler.deleteTimetable(info)
    }

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
            "0"
        )
    }
}