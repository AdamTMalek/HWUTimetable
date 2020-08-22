package com.github.hwutimetable

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.Scraper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_course_timetable.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCourseActivity : AddTimetableActivity<CourseTimetableScraper>() {
    private val selectedCourses = mutableSetOf<Option>()

    override fun setupView() {
        setContentView(R.layout.activity_add_course_timetable)
        setTitle(R.string.add_course_activity_title)
        setupCoursesListView()

        add_course_button.isEnabled = false
        setGroupsInputChangeListener()
        addAddCourseClickHandler()
    }

    private fun addAddCourseClickHandler() {
        add_course_button.setOnClickListener {
            addCourseToList()
        }
    }

    private fun setupCoursesListView() {
        courses_list.setHasFixedSize(false)
        courses_list.adapter = CourseListAdapter(selectedCourses)
        courses_list.adapter!!.notifyDataSetChanged()
        courses_list.layoutManager = LinearLayoutManager(this)
    }

    private fun setGroupsInputChangeListener() {
        groups_input.setOnItemClickListener { parent, view, position, id ->
            add_course_button.isEnabled = true
        }

        groups_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (groupOptions.none { it.text == s?.toString() })
                    add_course_button.isEnabled = false
            }
        })
    }

    override fun populateGroupsInput() {
        groups_input.setAdapter(
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                groupOptions.map { it.text })
        )
    }

    override fun filterGroupsBySemester() {
        val semesterRegex = Regex("[\\w .]+-[ ]?S([12])")
        val selectedSemester = semesterSpinner.selectedItem.toString().last()

        groupOptions.clearAndAddAll(groupOptions.filter {
            val courseSemester = semesterRegex.find(it.text)
                ?.groupValues
                ?.getOrNull(1)
                ?.first()
                ?: return@filter true

            courseSemester == selectedSemester
        })
    }

    override fun setGetTimetableClickListener() {
        getTimetable.setOnClickListener { }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedDepartment = departmentsOptions.find { it.text == departmentsSpinner.selectedItem.toString() }
            ?: return

        if (selectedDepartment.text == "(Any Department)")
            return

        val filter = Scraper.FilterBuilder()
            .withDepartment(selectedDepartment.optionValue)
            .getFilter()

        mainScope.launch {
            changeProgressBarVisibility(true)
            populateGroups(filter)
            changeProgressBarVisibility(false)
        }
    }

    private fun addCourseToList() {
        val selectedCourse = groups_input.text.toString()
        val groupOption = groupOptions.find { it.text == selectedCourse }
            ?: return
        selectedCourses.add(groupOption)
        courses_list.adapter!!.apply {
            notifyDataSetChanged()
        }
        setupCoursesListView()
    }
}
