package com.github.hwutimetable

import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import org.jsoup.nodes.Document
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TestScraper @Inject constructor() : ProgrammeTimetableScraper {
    private val backgroundCss = URL("file:///android_res/raw/activitytype.css")
    private val typeBackgroundProvider = TimetableClass.Type.OnlineBackgroundProvider(backgroundCss)

    val semesterOneGroup = "grp1 (Semester 1)"
    val semesterTwoGroup = "grp2 (Semester 2)"

    override suspend fun setup() {
        // We don't need to do anything
    }

    override fun getLevels(): List<Option> {
        return listOf(
            Option("dval0", "Semester 1"),
            Option("dval1", "Semester 2"),
            Option("dval2", "dep2")
        )
    }

    override fun getDepartments(): List<Option> {
        return listOf(
            Option("dval0", "(Any Department)"),
            Option("dval1", "dep1"),
            Option("dval2", "dep2")
        )
    }

    override suspend fun getGroups(filters: Map<String, Any>): List<Option> {
        return listOf(Option("gval0", semesterOneGroup), Option("gval1", semesterTwoGroup))
    }

    override suspend fun getTimetable(filters: Map<String, Any>): Document {
        val context = InstrumentationRegistry.getInstrumentation().context
        val input = context.resources.openRawResource(com.github.hwutimetable.test.R.raw.tt1)
        return SampleTimetableHandler(typeBackgroundProvider).getDocument(input)!!
    }
}