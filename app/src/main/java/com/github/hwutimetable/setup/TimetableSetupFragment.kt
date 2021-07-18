package com.github.hwutimetable.setup

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.github.hwutimetable.AddCourseActivity
import com.github.hwutimetable.AddProgrammeTimetableActivity
import com.github.hwutimetable.R
import com.github.hwutimetable.databinding.FragmentSetupTimetableBinding
import com.github.hwutimetable.network.NetworkUtilities
import com.github.hwutimetable.network.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimetableSetupFragment : Fragment(), NetworkUtilities.ConnectivityCallbackReceiver {
    private val connectivityCallback: NetworkUtilities.ConnectivityCallback by lazy {
        NetworkUtilities.ConnectivityCallback(requireContext())
    }

    @Inject
    lateinit var networkUtilities: NetworkUtils

    private lateinit var viewBinding: FragmentSetupTimetableBinding
    private lateinit var activityContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSetupTimetableBinding.inflate(inflater, container, false)
        setOnAddButtonPressed()
        setOnAddProgrammePressed()
        return viewBinding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
        connectivityCallback.registerCallbackReceiver(this)
    }

    override fun onStart() {
        super.onStart()

        if (!networkUtilities.hasInternetConnection())
            setAddTimetableButtonsEnable(false)
    }

    private fun setOnAddButtonPressed() {
        viewBinding.addCourseButton.setOnClickListener {
            val intent = Intent(requireView().context, AddCourseActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
        }
    }

    private fun setOnAddProgrammePressed() {
        viewBinding.addProgrammeButton.setOnClickListener {
            val intent = Intent(requireView().context, AddProgrammeTimetableActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
        }
    }

    override fun onConnectionAvailable() {
        (activityContext as Activity).runOnUiThread {
            setAddTimetableButtonsEnable(true)
        }
    }

    override fun onConnectionLost() {
        (activityContext as Activity).runOnUiThread {
            setAddTimetableButtonsEnable(false)
        }
    }

    private fun setAddTimetableButtonsEnable(enabled: Boolean) {
        requireView().findViewById<Button>(R.id.add_course_button).isEnabled = enabled
        requireView().findViewById<Button>(R.id.add_programme_button).isEnabled = enabled
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityCallback.cleanup()
    }
}