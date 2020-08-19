package com.github.hwutimetable.parser

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class OnlineBackgroundProviderTest {
    private val classLoader = javaClass.classLoader!!
    private val cssUrl = classLoader.getResource("activitytype.css")
    private val backgroundProvider = TimetableClass.Type.OnlineBackgroundProvider(cssUrl)

    @Test
    fun testClassTypeWithSpace() {
        // We expect the function to replace space with underscore.
        runBlocking {
            val expectedColor = "#222287"
            val actualColor = backgroundProvider.getBackgroundColor("All students")
            assertEquals(expectedColor, actualColor)
        }
    }

    @Test
    fun classTypeWithoutSpace() {
        // We expect the function to replace space with underscore.
        runBlocking {
            val expectedColor = "#FFCC00"
            val actualColor = backgroundProvider.getBackgroundColor("SGrp")
            assertEquals(expectedColor, actualColor)
        }
    }
}