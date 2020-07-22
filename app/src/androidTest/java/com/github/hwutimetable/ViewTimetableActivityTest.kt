package com.github.hwutimetable

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class ViewTimetableActivityTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val timetable: Timetable by lazy {
        val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
        val document = SampleTimetableHandler().getDocument(input)!!
        val parser = Parser(document)
        val days = parser.getTimetable()
        Timetable(days, Timetable.TimetableInfo("C01", "N01", Semester(parser.getSemesterStartDate(), 1)))
    }

    private lateinit var scenario: ActivityScenario<ViewTimetable>


    @Before
    fun setup() {
        val intent = Intent(targetContext, ViewTimetable::class.java)
        intent.putExtra("timetable", timetable)
        scenario = ActivityScenario.launch(intent)
    }

    @Test
    fun testTitleIsSet() {
        scenario.onActivity { activity ->
            val actualTitle = activity.toolbar.title
            assertEquals(timetable.info.name, actualTitle)
        }
    }
}