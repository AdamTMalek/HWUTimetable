package com.example.hwutimetable

import android.content.Context
import android.widget.TextView
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hwutimetable.di.FileModule
import com.example.hwutimetable.di.NetworkUtilitiesModule
import com.example.hwutimetable.filehandler.TimetableFileHandler
import com.example.hwutimetable.network.NetworkUtils
import com.example.hwutimetable.parser.Semester
import com.example.hwutimetable.parser.Timetable
import com.example.hwutimetable.settings.SettingsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.joda.time.LocalDate
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

@UninstallModules(value = [FileModule::class, NetworkUtilitiesModule::class])
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

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Inject
    lateinit var timetableFileHandler: TimetableFileHandler

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Before
    fun init() {
        hiltRule.inject()
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun populateInfoList() {
        fun getInfo(code: String): Timetable.TimetableInfo {
            val semester = Semester(LocalDate.now(), 1)
            return Timetable.TimetableInfo("C01", "Test", semester)
        }

        listOf(
            Timetable(emptyArray(), getInfo("C01")),
            Timetable(emptyArray(), getInfo("C02")),
            Timetable(emptyArray(), getInfo("C03"))
        ).forEach { timetable ->
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
            val addButton = activity.findViewById<FloatingActionButton>(R.id.fab)
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
            val addButton = activity.findViewById<FloatingActionButton>(R.id.fab)
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
            val addButton = activity.findViewById<FloatingActionButton>(R.id.fab)
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
            val addButton = activity.findViewById<FloatingActionButton>(R.id.fab)
            assertTrue(addButton.isEnabled)
        }
    }

    @Test
    fun testAddButtonStartsActivity() {
        with(networkUtils as TestNetworkUtilitiesModule.TestNetworkUtilities) {
            wifiOn = true
        }
        Intents.init()

        launchActivity()
        Espresso.onView(withId(R.id.fab))
            .perform(click())

        Intents.intended(IntentMatchers.hasComponent(AddActivity::class.java.name))
    }

    @Test
    fun testSettingsButtonStartsActivity() {
        Intents.init()
        launchActivity()

        // Open menu
        Espresso.openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().context)

        // Find settings menu entry (withId does not work)
        Espresso.onView(withText(R.string.action_settings))
            .perform(click())

        Intents.intended(IntentMatchers.hasComponent(SettingsActivity::class.java.name))
    }

    /**
     * Finish the activity and clean up the device state
     */
    @After
    fun cleanup() {
        scenario.close()
    }
}
