package com.github.hwutimetable

import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.hwutimetable.databinding.ActivityAddCourseTimetableBinding
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.parser.CourseTimetableParser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.CourseTimetableScraper
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.Scraper
import com.github.hwutimetable.validators.EmptyEditTextValidator
import com.github.hwutimetable.validators.FormValidator
import com.github.hwutimetable.validators.UniqueTimetableNameValidator
import com.github.hwutimetable.validators.Validator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.LocalTime

@AndroidEntryPoint
class AddCourseActivity : AddTimetableActivity<CourseTimetableScraper, ActivityAddCourseTimetableBinding>() {
    private val selectedCourses = mutableSetOf<Option>()

    override fun setupView() {
        setTitle(R.string.add_course_activity_title)
        setupCoursesListView()

        viewBinding.addCourseButton.isEnabled = false

        addValidatorsToViews()
        FormValidator(viewBinding.root, ::onFormValid, ::onFormInvalid)

        addAddCourseClickHandler()
    }

    override fun inflateViewBinding() = ActivityAddCourseTimetableBinding.inflate(layoutInflater)

    private fun addValidatorsToViews() {
        viewBinding.timetableName.addValidator(UniqueTimetableNameValidator(timetableHandler), EmptyEditTextValidator())
        viewBinding.coursesList.addValidator(CoursesListValidator())
    }

    private fun addAddCourseClickHandler() {
        viewBinding.addCourseButton.setOnClickListener {
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
        viewBinding.coursesList.let {
            it.setHasFixedSize(false)
            it.adapter = CourseListAdapter(selectedCourses)
            it.adapter!!.notifyDataSetChanged()
            it.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onGroupValidated(valid: Boolean) {
        viewBinding.addCourseButton.isEnabled = valid
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
        val selectedCourse = viewBinding.groupsInput.text.toString()
        val groupOption = groupOptions.find { it.text == selectedCourse }
            ?: return
        selectedCourses.add(groupOption)
        viewBinding.coursesList.adapter!!.apply {
            notifyDataSetChanged()
        }
    }

    private fun onFormValid() {
        viewBinding.getTimetable.isEnabled = true
    }

    private fun onFormInvalid() {
        viewBinding.getTimetable.isEnabled = false
    }

    private suspend fun getTimetable() {
        var semester: Semester? = null
        var startTime = LocalTime.MIDNIGHT  // Will be changed when parser is set

        resetFilter()

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
            startTime = parser.getDayStartTime()

            if (semester == null)
                semester = Semester(parser.getSemesterStartDate(), semesterNumber)

            scraper.setup()
            return@map timetable
        }

        val dateTimeStamp = currentDateProvider.getCurrentDateTime().toString("ddMMYYYYHHmm")
        val info = Timetable.Info(
            "GEN$dateTimeStamp", viewBinding.timetableName.text.toString(), semester!!,
            startTime, true
        )
        val timetable = Timetable.fromTimetables(info, timetableDays)

        if (isSaveTimetableChecked()) {
            saveTimetable(timetable)
        }

        changeProgressBarVisibility(false)
        startViewTimetableActivity(timetable)
    }

    private suspend fun resetFilter() {
        val filter = Scraper.FilterBuilder().getFilter()
        scraper.getGroups(filter)
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

    private class CoursesListValidator : Validator<RecyclerView> {
        override val errorString: String
            get() = "Your timetable needs to have at least one course."

        override fun validate(widget: RecyclerView): Boolean {
            val itemCount = widget.adapter?.itemCount ?: 0
            return itemCount > 0
        }
    }
}
