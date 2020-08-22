package com.github.hwutimetable

import android.view.View
import android.widget.AdapterView
import com.github.hwutimetable.scraper.CourseTimetableScraper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCourseActivity : AddTimetableActivity<CourseTimetableScraper>(), AdapterView.OnItemSelectedListener {
    override fun setupView() {
        TODO("Not yet implemented")
    }

    override fun filterGroupsBySemester() {
        TODO("Not yet implemented")
    }

    override fun populateGroupsInput() {
        TODO("Not yet implemented")
    }

    override fun setGetTimetableClickListener() {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

}
