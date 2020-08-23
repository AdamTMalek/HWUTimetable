package com.github.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.parser.ProgrammeTimetableParser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import com.github.hwutimetable.scraper.Scraper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add_programme_timetable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The [AddProgrammeTimetableActivity] is the activity for adding a programme timetable.
 * This is equivalent of the form on "Student Groups" site, hence [ProgrammeTimetableScraper]
 * is used.
 *
 * Annotated with @AndroidEntryPoint as the base class, [AddTimetableActivity], uses Hilt
 * and dependency injection.
 */
@AndroidEntryPoint
class AddProgrammeTimetableActivity : AddTimetableActivity<ProgrammeTimetableScraper>() {
    private var levelOptions = mutableListOf<Option>()

    /**
     * Sets the content view and the title of the activity.
     * This method will be called by [onCreate] by the base class.
     */
    override fun setupView() {
        setContentView(R.layout.activity_add_programme_timetable)
        setTitle(R.string.add_programme_activity_title)
    }

    /**
     * Sets the selection listener on all the spinners of the activity.
     */
    override fun setSpinnerSelectionListener() {
        super.setSpinnerSelectionListener()
        levels_spinner.onItemSelectedListener = this
    }

    override fun onGetTimetableButtonClick() {
        get_timetable.setOnClickListener {
            val groupOption = groupOptions.find {
                it.text == groups_spinner.selectedItem.toString()
            }!!

            val semester = getSemesterFromName((groupOption.text))

            mainScope.launch {
                changeProgressBarVisibility(true)
                getTimetable(groupOption, semester)
                changeProgressBarVisibility(false)
            }
        }
    }

    /**
     * Uses the given [name] to determine the semester.
     * @return semester as integer
     */
    private fun getSemesterFromName(name: String): Int {
        val regex = Regex("(?<=[Ss]emester )(\\d)")
        val match = regex.find(name) ?: throw IllegalStateException("Semester match is null!")
        return match.groups.first()!!.value.toInt()
    }

    /**
     * Populate the spinners of the activity.
     * We override it, as we need to populate level spinner as well.
     */
    override suspend fun populateSpinners() {
        super.populateSpinners()

        changeProgressBarVisibility(true)

        levelOptions.addAll(scraper.getLevels())
        populateSpinner(findViewById(R.id.levels_spinner), levelOptions.map { it.text })

        changeProgressBarVisibility(false)
    }

    /**
     * Filters only the groups that are for the selected semester.
     */
    override fun filterGroupsBySemester() {
        val selectedSemester = semester_spinner.selectedItem as String
        groupOptions.clearAndAddAll(groupOptions.filter { it.text.contains(selectedSemester, ignoreCase = true) })
    }

    /**
     * Populates the [groups_spinner] with the groups and enables the [get_timetable] button.
     */
    override fun populateGroupsInput() {
        populateSpinner(findViewById(R.id.groups_spinner), groupOptions.map { it.text })
        get_timetable.isEnabled = true
    }

    /**
     * Callback for all Spinners
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedDepartment = departmentsOptions.find { it.text == departments_spinner.selectedItem.toString() }
        val selectedLevel = levelOptions.find { it.text == levels_spinner.selectedItem.toString() }

        if (selectedDepartment == null || selectedLevel == null)
            return

        if (selectedDepartment.text == "(Any Department)" || selectedLevel.text == "(Any Level)")
            return // We require both filters

        val filter = Scraper.FilterBuilder()
            .withDepartment(selectedDepartment.optionValue)
            .withLevel(selectedLevel.optionValue)
            .getFilter()

        mainScope.launch {
            changeProgressBarVisibility(true)
            populateGroups(filter)
            changeProgressBarVisibility(false)
        }
    }

    /**
     * Called after clicking [get_timetable] button.
     * Scraps the timetable for the selected group and starts the [TimetableViewActivity].
     * If [isSaveTimetableChecked] returns `true`, the timetable will be saved on the device.
     */
    private suspend fun getTimetable(requestedGroup: Option, semesterNumber: Int) {
        val code = requestedGroup.optionValue
        val name = requestedGroup.text

        val filter = Scraper.FilterBuilder()
            .withGroup(code)
            .withSemester(semesterNumber)
            .getFilter()

        val document = scraper.getTimetable(filter)
        val parser = ProgrammeTimetableParser(document, TimetableClass.Type.OnlineBackgroundProvider())
        val timetableDays = parser.getTimetable()
        val semesterStartDate = parser.getSemesterStartDate()

        val semester = Semester(semesterStartDate, semesterNumber)
        val info = Timetable.Info(code, name, semester, false)
        val timetable = Timetable(timetableDays, info)

        if (isSaveTimetableChecked()) {
            withContext(Dispatchers.IO) {
                timetableHandler.save(timetable)
            }
        }

        changeProgressBarVisibility(false)

        val intent = Intent(this, TimetableViewActivity::class.java)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}
