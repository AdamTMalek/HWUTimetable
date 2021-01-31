package com.github.hwutimetable

import android.view.View
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager.widget.ViewPager
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import com.github.hwutimetable.setup.SetupActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@UninstallModules(
    value = [ProgrammeScraperModule::class, CourseScraperModule::class]
)
@HiltAndroidTest
class SetupActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<SetupActivity>
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun launchActivity(): ActivityScenario<SetupActivity> {
        return ActivityScenario.launch(SetupActivity::class.java).apply {
            scenario = this
        }
    }

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testTitleShowsSteps() {
        launchActivity()
        (1..3).forEach { step ->
            Espresso.onView(withSubstring(targetContext.getString(R.string.setup_activity_title)))
                .check(matches(withSubstring("($step/3)")))

            if (step == 3)  // Last step - next button should be invisible
                return@forEach
            Espresso.onView(withSubstring(targetContext.getString(R.string.next)))
                .perform(click())
        }
    }


    @Test
    fun testNextButtonGoesToNextFragment() {
        launchActivity().onActivity { activity ->
            val viewPager = getViewPager(activity)
            val currentPage = viewPager.currentItem
            activity.findViewById<Button>(R.id.next_button).performClick()

            assertEquals(currentPage + 1, viewPager.currentItem)
        }
    }

    @Test
    fun testNextButtonNotVisibleOnLastFragment() {
        launchActivity().onActivity { activity ->
            getViewPager(activity).run {
                currentItem = adapter!!.count - 1
            }

            val button = activity.findViewById<Button>(R.id.next_button)
            assertEquals(View.INVISIBLE, button.visibility)
        }
    }

    @Test
    fun testBackButtonGoesToPreviousFragment() {
        launchActivity().onActivity { activity ->
            val viewPager = getViewPager(activity)
            viewPager.currentItem = viewPager.currentItem + 1
            val currentPage = viewPager.currentItem

            activity.findViewById<Button>(R.id.back_button).performClick()

            assertEquals(currentPage - 1, viewPager.currentItem)
        }
    }

    @Test
    fun testBackButtonNotVisibleOnFirstFragment() {
        launchActivity().onActivity { activity ->
            getViewPager(activity).run {
                currentItem = 0
            }

            val button = activity.findViewById<Button>(R.id.back_button)
            assertEquals(View.INVISIBLE, button.visibility)
        }
    }

    @Test
    fun testAddCourseButtonWorksOnLastStep() {
        Intents.init()
        launchActivity().onActivity { activity ->
            getViewPager(activity).run {
                currentItem = adapter!!.count - 1
            }
            activity.findViewById<Button>(R.id.add_course_button).performClick()

        }
        Intents.intended(IntentMatchers.hasComponent(AddCourseActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testAddProgrammeButtonWorksOnLastStep() {
        Intents.init()
        launchActivity().onActivity { activity ->
            getViewPager(activity).run {
                currentItem = adapter!!.count - 1
            }

            activity.findViewById<Button>(R.id.add_programme_button).performClick()
        }
        Intents.intended(IntentMatchers.hasComponent(AddProgrammeTimetableActivity::class.java.name))
        Intents.release()
    }

    private fun getViewPager(activity: SetupActivity) = activity.findViewById<ViewPager>(R.id.view_pager)

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestProgrammeScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: TestScraper): ProgrammeTimetableScraper
    }

    @Module
    @InstallIn(ApplicationComponent::class)
    abstract class TestCourseScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: com.github.hwutimetable.TestCourseScraper): CourseTimetableScraper
    }
}