package com.github.hwutimetable

import android.view.View
import android.widget.AdapterView
import com.github.hwutimetable.databinding.ActivityAddProgrammeTimetableBinding
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.parser.ProgrammeTimetableParser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.ProgrammeTimetableScraper
import com.github.hwutimetable.scraper.Scraper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The [AddProgrammeTimetableActivity] is the activity for adding a programme timetable.
 * This is equivalent of the form on "Student Groups" site, hence [ProgrammeTimetableScraper]
 * is used.
 *
 * Annotated with @AndroidEntryPoint as the base class, [AddTimetableActivity], uses Hilt
 * and dependency injection.
 */
@AndroidEntryPoint
class AddProgrammeTimetableActivity :
    AddTimetableActivity<ProgrammeTimetableScraper, ActivityAddProgrammeTimetableBinding>() {
    private var levelOptions = mutableListOf<Option>()

    /**
     * Sets the content view and the title of the activity.
     * This method will be called by [onCreate] by the base class.
     */
    override fun setupView() {
        setTitle(R.string.add_programme_activity_title)
    }

    override fun inflateViewBinding() = ActivityAddProgrammeTimetableBinding.inflate(layoutInflater)

    override fun onGroupValidated(valid: Boolean) {
        viewBinding.getTimetable.isEnabled = valid
    }

    /**
     * Sets the selection listener on all the spinners of the activity.
     */
    override fun setSpinnerSelectionListener() {
        super.setSpinnerSelectionListener()
        viewBinding.levelsSpinner.onItemSelectedListener = this
    }

    override fun onGetTimetableButtonClick() {
        viewBinding.getTimetable.setOnClickListener {
            val groupOption = groupOptions.find {
                it.text == viewBinding.groupsInput.text.toString()
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
        val selectedSemester = viewBinding.semesterSpinner.selectedItem as String
        groupOptions.clearAndAddAll(groupOptions.filter { it.text.contains(selectedSemester, ignoreCase = true) })
    }

    /**
     * Callback for all Spinners
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedDepartment =
            departmentsOptions.find { it.text == viewBinding.departmentsSpinner.selectedItem.toString() }
        val selectedLevel = levelOptions.find { it.text == viewBinding.levelsSpinner.selectedItem.toString() }

        if (selectedDepartment == null || selectedLevel == null)
            return

        val filterBuilder = Scraper.FilterBuilder()

        if (selectedDepartment.text != "(Any Department)")
            filterBuilder.withDepartment(selectedDepartment.optionValue)

        if (selectedDepartment.text != "(Any Level)")
            filterBuilder.withLevel(selectedLevel.optionValue)

        val filter = filterBuilder.getFilter()

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
        val info = Timetable.Info(code, name, semester, parser.getDayStartTime(), false)
        val timetable = Timetable(timetableDays, info)

        if (isSaveTimetableChecked()) {
            saveTimetable(timetable)
        }

        changeProgressBarVisibility(false)
        startViewTimetableActivity(timetable)
    }
}
