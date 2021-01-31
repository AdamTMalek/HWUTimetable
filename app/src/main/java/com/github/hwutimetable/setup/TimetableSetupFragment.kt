package com.github.hwutimetable.setup

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.hwutimetable.AddCourseActivity
import com.github.hwutimetable.AddProgrammeTimetableActivity
import com.github.hwutimetable.databinding.FragmentSetupTimetableBinding

class TimetableSetupFragment : Fragment() {
    private lateinit var viewBinding: FragmentSetupTimetableBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentSetupTimetableBinding.inflate(inflater, container, false)
        setOnAddButtonPressed()
        setOnAddProgrammePressed()
        return viewBinding.root
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
}