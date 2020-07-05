package com.example.hwutimetable.filehandler

import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class InfoFileTest {
    companion object {
        private val dir_path: String = System.getProperty("user.dir") + "/if_test_dir"
        private val test_dir = File(dir_path)
        private lateinit var infoFile: InfoFile

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
            infoFile = InfoFile(test_dir)
        }

        @AfterClass
        @JvmStatic
        fun cleanUp() {
            if (!test_dir.exists())
                fail("The timetable file handler test directory does not exist before cleaning")

            val file = File(test_dir, InfoFile.FILENAME)
            file.delete()

            test_dir.delete()
        }
    }

    @Before
    fun deleteAllInfo() {
        val file = File(test_dir, InfoFile.FILENAME)
        if (file.exists())
            file.delete()
    }

    private fun addTestInfoWithSaveAll(): List<TimetableInfo> {
        val infoList = listOf(
            TimetableInfo("XXX", "NAME", 1),
            TimetableInfo("YYY", "NAME", 2)
        )

        infoFile.saveAll(infoList)
        return infoList
    }

    @Test
    fun testSavesInfo() {
        val info = TimetableInfo("XXX", "NAME", 1)
        infoFile.save(info)

        assertEquals(info, infoFile.getList().firstOrNull())
    }

    @Test
    fun testDoesNotDuplicateWhenSaving() {
        val infoA = TimetableInfo("XXX", "NAME", 1)
        val infoB = TimetableInfo("XXX", "NAME", 2)
        infoFile.save(infoA)
        infoFile.save(infoA) // Deliberate duplication
        infoFile.save(infoB)

        val infoList = listOf(infoA, infoB)

        assertTrue(infoList == infoFile.getList())
    }

    @Test
    fun testSavesAll() {
        val expectedList = addTestInfoWithSaveAll()
        assertTrue(expectedList == infoFile.getList())
    }

    @Test
    fun testDelete() {
        val list = addTestInfoWithSaveAll()
        val infoToDelete = list.first()
        val originalSize = list.size
        val expectedSize = originalSize - 1
        infoFile.delete(infoToDelete)

        assertEquals(expectedSize, infoFile.getList().size)
    }

    @Test
    fun testGetInfoByName() {
        val list = addTestInfoWithSaveAll()
        val testInfo = list.first()
        val returnedInfo = infoFile.getInfoByName(testInfo.name)

        assertEquals(testInfo, returnedInfo)
    }

    @Test
    fun testGetInfoByNameWhenNotFound() {
        assertNull(infoFile.getInfoByName("_"))
    }

    @Test
    fun testGetInfoByCode() {
        val list = addTestInfoWithSaveAll()
        val testInfo = list.first()
        val returnedInfo = infoFile.getInfoByCode(testInfo.code)

        assertEquals(testInfo, returnedInfo)
    }

    @Test
    fun testGetInfoByCodeWhenNotFound() {
        assertNull(infoFile.getInfoByCode("_"))
    }
}