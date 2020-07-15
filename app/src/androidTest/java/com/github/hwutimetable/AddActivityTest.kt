package com.github.hwutimetable

import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import com.github.hwutimetable.di.TimetableScraperModule
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.TimetableScraper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.assertTrue
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(TimetableScraperModule::class)
@HiltAndroidTest
class AddActivityTest {
    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TimetableScraperTestModule {
        @Binds
        abstract fun bindScraper(scraperForTest: ScraperForTest): TimetableScraper
    }

    @Singleton
    class ScraperForTest @Inject constructor() : TimetableScraper {
        override suspend fun setup() {
            // We don't need to do anything
        }

        override fun getDepartments(): List<Option> {
            return listOf(
                Option("dval1", "dep1"),
                Option("dval2", "dep2")
            )
        }

        override fun getLevels(): List<Option> {
            return listOf(
                Option("lval1", "lev1"),
                Option("lval2", "lev2")
            )
        }

        override suspend fun getGroups(department: String, level: String): List<Option> {
            return listOf(
                Option("gval1", "grp1"),
                Option("gval2", "grp2")
            )
        }

        override suspend fun getTimetable(group: String, semester: Int): Document {
            val file = File("src/test/resources/sampleTimetables/tt1.html")
            return SampleTimetableHandler().getDocument(file)!!
        }
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    private lateinit var scenario: ActivityScenario<AddActivity>

    @Before
    fun init() {
        hiltRule.inject()
        scenario = ActivityScenario.launch(AddActivity::class.java)
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun testDepartmentsListGetsPopulated() {
        scenario.onActivity { activity ->
            val departmentsSpinner = activity.findViewById<Spinner>(R.id.departments_spinner)
            assertTrue(departmentsSpinner.adapter.count > 0)
        }
    }

    @Test
    fun testLevelsListGetsPopulated() {
        scenario.onActivity { activity ->
            val levelsSpinner = activity.findViewById<Spinner>(R.id.levels_spinner)
            assertTrue(levelsSpinner.adapter.count > 0)
        }
    }
}