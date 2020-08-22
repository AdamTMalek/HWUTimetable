package com.github.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.parser.CourseTimetableParser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
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

    override fun onGetTimetableButtonClick() {
        mainScope.launch {
            changeProgressBarVisibility(true)
            getTimetable()
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
        get_timetable.isEnabled = true
    }

    private suspend fun getTimetable() {
        var semester: Semester? = null

        val timetableDays = selectedCourses.map { course ->
            val code = getCourseCode(course)
            val name = getCourseName(course)
            val semesterNumber = getSemesterFromCode(code)

            val filter = Scraper.FilterBuilder()
                .withGroup(code)
                .withSemester(semesterNumber)
                .getFilter()

            val document = scraper.getTimetable(filter)
            val parser = CourseTimetableParser(code, name, document, TimetableClass.Type.OnlineBackgroundProvider())

            val timetable = parser.getTimetable()

            if (semester == null)
                semester = Semester(parser.getSemesterStartDate(), semesterNumber)

            scraper.setup()
            return@map timetable
        }

        val info = Timetable.Info("GEN", "App-Generated", semester!!)
        val timetable = Timetable.fromTimetables(info, timetableDays)

        changeProgressBarVisibility(false)

        val intent = Intent(this, TimetableViewActivity::class.java)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun getSemesterFromCode(courseCode: String): Int {
        val regex = Regex("S([12])")
        val match = regex.find(courseCode)
            ?.groupValues
            ?.get(1)

        return match?.toInt()
            ?: when (semesterSpinner.selectedItem.toString()) {
                getString(R.string.semester_1) -> 1
                getString(R.string.semester_2) -> 2
                else -> getClosestSemester()
            }
    }

    private fun getCourseCode(courseOption: Option) = courseOption.optionValue

    private fun getCourseName(courseOption: Option): String {
        val code = getCourseCode(courseOption)
        return courseOption.text.removePrefix(code).trim { it.isWhitespace() || it == '-' }
    }
}
