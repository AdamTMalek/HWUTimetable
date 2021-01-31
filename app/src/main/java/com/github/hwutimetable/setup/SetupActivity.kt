package com.github.hwutimetable.setup

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.hwutimetable.R
import com.github.hwutimetable.databinding.SetupActivityBinding
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import com.github.hwutimetable.settings.UpdatePreferenceFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * The [SetupActivity] is an activity containing a number of fragments
 * that are used to introduce the user to the application and setup
 * the preferences.
 */
@AndroidEntryPoint
class SetupActivity : AppCompatActivity(), NetworkUtilities.ConnectivityCallbackReceiver {
    private var pagerAdapter = PagerAdapter(supportFragmentManager)
    private var currentStep = 1
    private val totalSetupSteps = 3

    private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
        NetworkUtilities.ConnectivityCallback(this)
    }

    @Inject
    lateinit var networkUtilities: NetworkUtils

    private lateinit var viewBinding: SetupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = SetupActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.viewPager.adapter = pagerAdapter
        setToolbarTitle()
        setOnPageChangeListener()
        setOnNextClickHandler()
        setOnBackClickHandler()
        connectivityCallback.registerCallbackReceiver(this)
    }

    private fun setOnPageChangeListener() {
        viewBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                currentStep = position + 1
                setToolbarTitle()
                setStepDescription()
                setBackButtonVisibility()
                setNextButtonVisibility()

                if (currentStep == totalSetupSteps) {
                    if (!networkUtilities.hasInternetConnection())
                        setAddTimetableButtonsEnable(false)
                }
            }

            private fun setBackButtonVisibility() {
                viewBinding.backButton.visibility = if (currentStep == 1)
                    View.INVISIBLE
                else
                    View.VISIBLE
            }

            private fun setNextButtonVisibility() {
                viewBinding.nextButton.visibility = if (currentStep == totalSetupSteps)
                    View.INVISIBLE
                else
                    View.VISIBLE
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }

    private fun setOnNextClickHandler() {
        viewBinding.nextButton.setOnClickListener {
            with(viewBinding.viewPager) {
                setCurrentItem(currentItem + 1, true)
            }
        }
    }

    private fun setOnBackClickHandler() {
        viewBinding.backButton.setOnClickListener {
            with(viewBinding.viewPager) {
                setCurrentItem(currentItem - 1, true)
            }
        }
    }

    private fun setToolbarTitle() {
        title = "${getString(R.string.setup_activity_title)} ($currentStep/$totalSetupSteps)"
    }

    private fun setStepDescription() {
        val textId = when (currentStep) {
            1 -> R.string.setup_step_1
            2 -> R.string.setup_step_2
            3 -> if (networkUtilities.hasInternetConnection()) R.string.setup_step_3 else R.string.setup_step_3_no_internet
            else -> throw IllegalArgumentException("Current step exceeded total steps")
        }

        viewBinding.stepDescription.text = getText(textId)
    }

    override fun onConnectionAvailable() {
        if (currentStep != totalSetupSteps)
            return

        runOnUiThread {
            viewBinding.stepDescription.text = getText(R.string.setup_step_3)
            setAddTimetableButtonsEnable(true)
        }
    }

    override fun onConnectionLost() {
        if (currentStep != totalSetupSteps)
            return

        runOnUiThread {
            viewBinding.stepDescription.text = getText(R.string.setup_step_3_no_internet)
            setAddTimetableButtonsEnable(false)
        }
    }

    private fun setAddTimetableButtonsEnable(enabled: Boolean) {
        findViewById<Button>(R.id.add_course_button).isEnabled = enabled
        findViewById<Button>(R.id.add_programme_button).isEnabled = enabled
    }

    override fun onDestroy() {
        connectivityCallback.cleanup()
        super.onDestroy()
    }

    inner class PagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> UpdatePreferenceFragment()
                1 -> ViewSetupFragment()
                2 -> TimetableSetupFragment()
                else -> throw IllegalArgumentException("Position exceeded setup steps")
            }
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_UNCHANGED
        }

        override fun getCount() = totalSetupSteps
    }
}