package com.github.hwutimetable

import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.Option
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TestCourseScraper @Inject constructor() : CourseTimetableScraper {
    private val backgroundProvider = object : TimetableClass.Type.BackgroundProvider {
        override suspend fun getBackgroundColor(type: String) = "#FFFFFF"
    }
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val timetableDocument: Document by lazy {
        val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
        SampleTimetableHandler(backgroundProvider).getDocument(input)!!
    }

    override suspend fun setup() {
        // There's nothing we have to do in here
    }

    override fun getDepartments(): List<Option> {
        return listOf(
            Option("val0", "Department 0"),
            Option("val1", "Department 1")
        )
    }

    override suspend fun getGroups(filters: Map<String, Any>): List<Option> {
        return listOf(
            Option("val0", "C00AA-S1"),
            Option("val1", "C00AA-S2")
        )
    }

    override suspend fun getTimetable(filters: Map<String, Any>): Document {
        return timetableDocument
    }
}