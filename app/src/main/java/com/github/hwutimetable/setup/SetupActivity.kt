package com.github.hwutimetable.setup

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.hwutimetable.R
import com.github.hwutimetable.databinding.SetupActivityBinding
import com.github.hwutimetable.settings.UpdatePreferenceFragment


/**
 * The [SetupActivity] is an activity containing a number of fragments
 * that are used to introduce the user to the application and setup
 * the preferences.
 */
class SetupActivity : AppCompatActivity() {
    private var pagerAdapter = PagerAdapter(supportFragmentManager)
    private val totalSetupSteps = 3

    private lateinit var viewBinding: SetupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = SetupActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.viewPager.adapter = pagerAdapter
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setToolbarTitle(currentStep = 1)
        setOnPageChangeListener()
        setOnNextClickHandler()
        setOnBackClickHandler()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setOnPageChangeListener() {
        viewBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val currentStep = position + 1
                setToolbarTitle(currentStep)
                setStepDescription(currentStep)
                setBackButtonVisibility(currentStep)
                setNextButtonVisibility(currentStep)
            }

            private fun setBackButtonVisibility(currentStep: Int) {
                viewBinding.backButton.visibility = if (currentStep == 1)
                    View.INVISIBLE
                else
                    View.VISIBLE
            }

            private fun setNextButtonVisibility(currentStep: Int) {
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

    private fun setToolbarTitle(currentStep: Int) {
        title = "${getString(R.string.setup_activity_title)} ($currentStep/$totalSetupSteps)"
    }

    private fun setStepDescription(currentStep: Int) {
        val textId = when (currentStep) {
            1 -> R.string.setup_step_1
            2 -> R.string.setup_step_2
            3 -> R.string.setup_step_3
            else -> throw IllegalArgumentException("Current step exceeded total steps")
        }

        viewBinding.stepDescription.text = getText(textId)
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