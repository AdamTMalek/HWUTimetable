package com.github.hwutimetable

import android.content.Context
import android.widget.TextView
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.di.CourseScraperModule
import com.github.hwutimetable.di.FileModule
import com.github.hwutimetable.di.NetworkUtilitiesModule
import com.github.hwutimetable.di.ProgrammeScraperModule
import com.github.hwutimetable.extensions.getSharedPreferences
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import com.github.hwutimetable.settings.SettingsActivity
import com.github.hwutimetable.setup.SetupActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.TestCase.*
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@UninstallModules(
    value = [FileModule::class, NetworkUtilitiesModule::class, ProgrammeScraperModule::class,
        CourseScraperModule::class]
)
@HiltAndroidTest
class MainActivityTest {
    @InstallIn(ApplicationComponent::class)
    @Module
    object TestTimetableFileHandlerModule {
        @Provides
        fun provideDirectory(@ApplicationContext context: Context): File {
            return File(context.filesDir, "/test/")
        }
    }

    @InstallIn(ApplicationComponent::class)
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

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Inject
    lateinit var timetableFileHandler: TimetableFileHandler

    @Inject
    lateinit var networkUtils: NetworkUtils

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun init() {
        hiltRule.inject()
        with(targetContext.getSharedPreferences(R.string.shared_pref_file_key, Context.MODE_PRIVATE).edit()) {
            putBoolean(targetContext.getString(R.string.first_run), false)
        }
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /**
     * Populates the info list with 3 timetables. Each named Timetable [1, 2 or 3]
     */
    private fun populateInfoList() {
        fun getInfo(timetableNumber: Int): Timetable.Info {
            val semester = Semester(LocalDate.now(), 1)
            val code = "C${timetableNumber.toString().padStart(2, '0')}"
            return Timetable.Info(code, "Timetable $timetableNumber", semester, LocalTime.parse("9:00"), false)
        }

        val timetables = sequence {
            var number = 1
            while (true) {
                yield(Timetable(emptyArray(), getInfo(number)))
                number++
            }
        }

        timetables.take(3).forEach { timetable ->
            timetableFileHandler.save(timetable)
        }
    }

    @Before
    fun clearInfoFile() {
        timetableFileHandler.deleteAllTimetables()
    }

    @Test
    fun checkTimetablesListedWhenEmpty() {
        launchActivity()
        scenario.onActivity { activity ->
            val list = activity.findViewById<RecyclerView>(R.id.recycler_view)
            assertTrue(list.isEmpty())
        }
    }

    @Test
    fun checkTimetablesListed() {
        populateInfoList()
        launchActivity()
        scenario.onActivity { activity ->
            val list = activity.findViewById<RecyclerView>(R.id.recycler_view)
            assertTrue(list.isNotEmpty())
        }
    }

    @Test
    fun testHelpTextVisibleWhenNoTimetables() {
        launchActivity()
        scenario.onActivity { activity ->
            val textView = activity.findViewById<TextView>(R.id.no_timetables_text)
            assertTrue(textView.isVisible)
        }
    }

    @Test
    fun testHelpTextInvisibleWhenTimetablesArePresent() {
        populateInfoList()
        launchActivity()
        scenario.onActivity { activity ->
            val textView = activity.findViewById<TextView>(R.id.no_timetables_text)
            assertFalse(textView.isVisible)
        }
    }

    @Test
    fun testAddButtonEnabledWhenHasInternetAccess() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            dataOn = false
            wifiOn = false
        }

        launchActivity()
        scenario.onActivity { activity ->
            val addButton = activity.findViewById<ExtendedFloatingActionButton>(R.id.add_timetable)
            assertFalse(addButton.isEnabled)
        }
    }

    @Test
    fun testAddButtonDisabledWhenNoInternetAccess() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            dataOn = false
            wifiOn = true
        }

        launchActivity()
        scenario.onActivity { activity ->
            val addButton = activity.findViewById<ExtendedFloatingActionButton>(R.id.add_timetable)
            assertTrue(addButton.isEnabled)
        }
    }

    @Test
    fun testAddButtonDisabledWhenInternetAccessLost() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            dataOn = false
            wifiOn = true
        }

        launchActivity()
        scenario.onActivity { activity ->
            activity.onConnectionLost()
            val addButton = activity.findViewById<ExtendedFloatingActionButton>(R.id.add_timetable)
            assertFalse(addButton.isEnabled)
        }
    }

    @Test
    fun testAddButtonEnabledWhenInternetAccessGained() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            dataOn = false
            wifiOn = false
        }

        launchActivity()
        scenario.onActivity { activity ->
            activity.onConnectionAvailable()
            val addButton = activity.findViewById<ExtendedFloatingActionButton>(R.id.add_timetable)
            assertTrue(addButton.isEnabled)
        }
    }

    @Test
    fun testAddProgrammeStartsActivity() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            wifiOn = true
        }
        Intents.init()

        launchActivity()
        Espresso.onView(withId(R.id.add_timetable))
            .perform(click())
        Espresso.onView(withId(R.id.add_programme))
            .perform(click())

        Intents.intended(IntentMatchers.hasComponent(AddProgrammeTimetableActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun testAddCourseStartsActivity() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            wifiOn = true
        }
        Intents.init()

        launchActivity()
        Espresso.onView(withId(R.id.add_timetable))
            .perform(click())
        Espresso.onView(withId(R.id.add_course))
            .perform(click())

        Intents.intended(IntentMatchers.hasComponent(AddCourseActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun testSettingsButtonStartsActivity() {
        Intents.init()
        launchActivity()

        // Open menu
        Espresso.openActionBarOverflowOrOptionsMenu(context)

        // Find settings menu entry (withId does not work)
        Espresso.onView(withText(R.string.action_settings))
            .perform(click())

        Intents.intended(IntentMatchers.hasComponent(SettingsActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun testSwipeToDelete() {
        populateInfoList()
        launchActivity()

        Espresso.onView(withText("Timetable 2")).perform(swipeLeft())
        assertNull(timetableFileHandler.getStoredTimetables().find { it.name == "Timetable 2" })
    }

    @Test
    fun testDeleteAllDisplaysAlertDialog() {
        launchActivity()
        // Open menu
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        // Find delete all menu entry
        Espresso.onView(withText(R.string.delete_all))
            .perform(click())

        // Check if displayed
        Espresso.onView(withText(R.string.delete_all_confirmation))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDeleteAllWhenYesClicked() {
        populateInfoList()
        launchActivity()

        // Get the dialog to display, click on the OK button
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        Espresso.onView(withText(R.string.delete_all)).perform(click())
        Espresso.onView(withId(android.R.id.button1)).perform(click())

        Espresso.onView(withId(R.id.recycler_view)).check { view, _ ->
            view as RecyclerView
            assertTrue(view.isEmpty())
        }
    }

    @Test
    fun testDeleteAllWhenNoClicked() {
        populateInfoList()
        launchActivity()

        // Get the dialog to display, click on the OK button
        Espresso.openActionBarOverflowOrOptionsMenu(context)
        Espresso.onView(withText(R.string.delete_all)).perform(click())
        Espresso.onView(withId(android.R.id.button2)).perform(click())

        Espresso.onView(withId(R.id.recycler_view)).check { view, _ ->
            view as RecyclerView
            assertTrue(view.isNotEmpty())
        }
    }

    @Test
    fun testRenameDialogPopsUp() {
        populateInfoList()
        launchActivity()

        Espresso.onView(withText("Timetable 2")).perform(longClick())
        Espresso.onView(withText(targetContext.getString(R.string.item_context_menu_rename))).perform(click())
        Espresso.onView(withText(targetContext.getString(R.string.rename_dialog_title))).check(matches(isDisplayed()))
    }

    @Test
    fun testRenameDialogCancel() {
        populateInfoList()
        launchActivity()

        Espresso.onView(withText("Timetable 2")).perform(longClick())
        Espresso.onView(withText(targetContext.getString(R.string.item_context_menu_rename))).perform(click())
        Espresso.onView(withText("Cancel")).perform(click())
        Espresso.onView(withText(targetContext.getString(R.string.rename_dialog_title))).check(doesNotExist())
    }

    @Test
    fun testRenameDialogRenameWorksInActivity() {
        populateInfoList()
        launchActivity()

        Espresso.onView(withText("Timetable 2")).perform(longClick())
        Espresso.onView(withText(targetContext.getString(R.string.item_context_menu_rename))).perform(click())
        Espresso.onView(withId(R.id.edit_timetable_title)).perform(clearText(), typeText("Renamed Timetable"))
        Espresso.onView(withText("Rename")).perform(click())

        Espresso.onView(withText("Renamed Timetable")).check(matches(isDisplayed()))
    }

    @Test
    fun testRenameUpdatesInfo() {
        populateInfoList()
        launchActivity()

        Espresso.onView(withText("Timetable 2")).perform(longClick())
        Espresso.onView(withText(targetContext.getString(R.string.item_context_menu_rename))).perform(click())
        Espresso.onView(withId(R.id.edit_timetable_title)).perform(clearText(), typeText("Renamed Timetable"))
        Espresso.onView(withText("Rename")).perform(click())

        assertNotNull(timetableFileHandler.getStoredTimetables().find { it.name == "Renamed Timetable" })
    }

    @Test
    fun testGoesToSetupOnFirstRun() {
        val sharedPrefs = targetContext.getSharedPreferences(R.string.shared_pref_file_key, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove(targetContext.getString(R.string.first_run))
            commit()
        }

        Intents.init()
        launchActivity()
        Intents.intended(IntentMatchers.hasComponent(SetupActivity::class.java.name))
        Intents.release()
    }

    /**
     * Finish the activity and clean up the device state
     */
    @After
    fun cleanup() {
        if (this::scenario.isInitialized)
            scenario.close()
    }
}
