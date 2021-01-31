package com.github.hwutimetable

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.github.hwutimetable.settings.SettingsActivity
import com.github.hwutimetable.setup.SetupActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class SettingsActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var scenario: ActivityScenario<SettingsActivity>

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun launchActivity(): ActivityScenario<SettingsActivity> {
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
        return scenario
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testRecentChangesClickShowsChangeLogDialog() {
        launchActivity()
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Recent changes")), click()
                )
            )

        onView(withSubstring(targetContext.getString(R.string.changelog_dialog_title)))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testRunSetupButtonStartsSetupActivity() {
        launchActivity()
        Intents.init()

        val runSetupTitle = targetContext.getString(R.string.run_setup_title)
        onView(withId(androidx.preference.R.id.recycler_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(runSetupTitle)),
                    click()
                )
            )

        Intents.intended(IntentMatchers.hasComponent(SetupActivity::class.java.name))
        Intents.release()
    }

    @After
    fun cleanup() {
        if (this::scenario.isInitialized) {
            scenario.close()
        }
    }
}