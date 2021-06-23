package com.github.hwutimetable

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.di.NetworkUtilitiesModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import com.github.hwutimetable.setup.SetupActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton


@UninstallModules(
    value = [NetworkUtilitiesModule::class, ProgrammeScraperModule::class, CourseScraperModule::class]
)
@HiltAndroidTest
class SetupActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var injectedNetworkUtils: NetworkUtils

    private val networkUtils
        get() = (injectedNetworkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities)

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
            goToLastSetupStep(activity)

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
            goToLastSetupStep(activity)
            activity.findViewById<Button>(R.id.add_course_button).performClick()
        }

        Intents.intended(IntentMatchers.hasComponent(AddCourseActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testAddProgrammeButtonWorksOnLastStep() {
        Intents.init()
        launchActivity().onActivity { activity ->
            goToLastSetupStep(activity)
            activity.findViewById<Button>(R.id.add_programme_button).performClick()
        }

        Intents.intended(IntentMatchers.hasComponent(AddProgrammeTimetableActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testAddTimetabledIfHasInternet() {
        networkUtils.wifiOn = true

        launchActivity().onActivity { activity ->
            goToLastSetupStep(activity)
            val addCourseButton = activity.findViewById<Button>(R.id.add_course_button)
            val addProgrammeButton = activity.findViewById<Button>(R.id.add_programme_button)

            assertTrue(addCourseButton.isEnabled)
            assertTrue(addProgrammeButton.isEnabled)
        }
    }

    @Test
    fun testAddTimetableButtonsDisabledIfNoInternet() {
        networkUtils.wifiOn = false
        networkUtils.dataOn = false

        launchActivity().onActivity { activity ->
            goToLastSetupStep(activity)
            val addCourseButton = activity.findViewById<Button>(R.id.add_course_button)
            val addProgrammeButton = activity.findViewById<Button>(R.id.add_programme_button)

            assertFalse(addCourseButton.isEnabled)
            assertFalse(addProgrammeButton.isEnabled)
        }
    }

    @Test
    fun testAddTimetableDescriptionIfHasInternet() {
        networkUtils.wifiOn = true

        launchActivity().onActivity { activity ->
            goToLastSetupStep(activity)
            val expectedDescription = activity.getString(R.string.setup_step_3)
            val actualDescription = activity.findViewById<TextView>(R.id.step_description).text

            assertEquals(expectedDescription, actualDescription)
        }
    }

    @Test
    fun testAddTimetableDescriptionIfNoInternet() {
        networkUtils.wifiOn = false
        networkUtils.dataOn = false

        launchActivity().onActivity { activity ->
            goToLastSetupStep(activity)
            val expectedDescription = activity.getString(R.string.setup_step_3_no_internet)
            val actualDescription = activity.findViewById<TextView>(R.id.step_description).text

            assertEquals(expectedDescription, actualDescription)
        }
    }

    private fun getViewPager(activity: SetupActivity) = activity.findViewById<ViewPager2>(R.id.view_pager)

    private fun goToLastSetupStep(activity: SetupActivity) {
        getViewPager(activity).run {
            currentItem = adapter!!.itemCount - 1
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestProgrammeScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: TestScraper): ProgrammeTimetableScraper
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class TestCourseScraper {
        @Binds
        abstract fun bindScraper(scraperForTest: com.github.hwutimetable.TestCourseScraper): CourseTimetableScraper
    }

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class TestNetworkUtilitiesModule {
        class TestNetworkUtilities @Inject constructor() : NetworkUtils {

            var wifiOn = false
            var dataOn = false

            override fun hasInternetConnection(): Boolean {
                return wifiOn || dataOn
            }

            override fun isWifiEnabled(): Boolean {
                return wifiOn
            }

            override fun isMobileDataEnabled(): Boolean {
                return dataOn
            }
        }

        @Singleton
        @Binds
        abstract fun bindNetworkUtilities(networkUtilities: TestNetworkUtilities): NetworkUtils
    }
}