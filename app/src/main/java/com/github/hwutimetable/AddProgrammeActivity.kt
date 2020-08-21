package com.github.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Parser
import com.github.hwutimetable.parser.Semester
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.parser.TimetableClass
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.ProgrammeScraper
import com.github.hwutimetable.scraper.Scraper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AddProgrammeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val mainScope = MainScope()

    @Inject
    lateinit var scraper: ProgrammeScraper

    @Inject
    lateinit var timetableHandler: TimetableFileHandler

    @Inject
    lateinit var currentDateProvider: CurrentDateProvider
    private var departments: List<Option>? = null
    private var levels: List<Option>? = null
    private var groups: List<Option>? = null
    private var selectedDepartment: Option? = null
    private var selectedLevel: Option? = null
    private var requestedGroup: Option? = null
    private var semester: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        setItemSelectedListener()
        setButtonClickListener()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.add_activity_title)

        mainScope.launch {
            populateDepartmentsAndLevels()
        }

        setClosestSemester()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Sets the ItemSelectedListener of all spinners of this activity to this object
     */
    private fun setItemSelectedListener() {
        departments_spinner.onItemSelectedListener = this
        levels_spinner.onItemSelectedListener = this
        semester_spinner.onItemSelectedListener = this
    }

    private fun setButtonClickListener() {
        submit_button.setOnClickListener {
            val groupOption = groups?.find {
                it.text == groups_spinner.selectedItem.toString()
            }

            if (groupOption != null) {
                requestedGroup = groupOption
                semester = getSemesterFromName((groupOption.text))

                mainScope.launch {
                    changeProgressBarVisibility(true)
                    getTimetable()
                    changeProgressBarVisibility(false)
                }
            }
        }
    }

    private fun getSemesterFromName(name: String): Int {
        val regex = Regex("(?<=[Ss]emester )(\\d)")
        val match = regex.find(name) ?: throw IllegalStateException("Semester match is null!")
        return match.groups.first()!!.value.toInt()
    }

    /**
     * Initialisation callback for AsyncScraper initialisation
     */
    private suspend fun populateDepartmentsAndLevels() {
        changeProgressBarVisibility(true)

        scraper.setup()
        departments = scraper.getDepartments()
        levels = scraper.getLevels()

        // Put the data we just got to the Spinners
        departments?.map { it.text }?.let { applyAdapterFromList(departments_spinner, it) }
        levels?.map { it.text }?.let { applyAdapterFromList(levels_spinner, it) }

        changeProgressBarVisibility(false)
    }

    private fun setClosestSemester() {
        val currentDate = currentDateProvider.getCurrentDate()
        val closetSemester = if (currentDate.monthOfYear >= 6)
            1
        else
            2

        semester_spinner.setSelection(closetSemester - 1)
    }

    /**
     * getGroups callback for AsyncScraper
     */
    private suspend fun getGroups() {
        val selectedSemester = semester_spinner.selectedItem as String
        groups = scraper.getGroups(
            mapOf(
                "department" to selectedDepartment!!.optionValue,
                "level" to selectedLevel!!.optionValue
            )
        )

        if (groups == null)
            return

        // Apply semester filter
        if (selectedSemester != getString(R.string.semester_any))
            groups = groups!!.filter { it.text.contains(selectedSemester, true) }

        applyAdapterFromList(groups_spinner, groups!!.map { it.text })
        submit_button.isEnabled = true
    }

    /**
     * Applies ArrayAdapter to the given list of strings and sets it to the given spinner
     * @param spinner: Spinner to which the new adapter should be set
     * @param list: List of strings
     */
    private fun applyAdapterFromList(spinner: Spinner, list: List<String>) {
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list)
    }

    /**
     * Callback for all Spinners
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            departments_spinner.id -> selectedDepartment = departments?.find {
                it.text == departments_spinner.selectedItem.toString()
            }
            levels_spinner.id -> selectedLevel = levels?.find {
                it.text == levels_spinner.selectedItem.toString()
            }
        }

        if (selectedDepartment == null || selectedLevel == null)
            return

        if (selectedDepartment!!.text == "(Any Department)" || selectedLevel!!.text == "(Any Level)")
            return // We require both filters

        mainScope.launch {
            changeProgressBarVisibility(true)
            getGroups()
            changeProgressBarVisibility(false)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    private suspend fun getTimetable() {
        checkNotNull(requestedGroup) { "requestedGroup cannot be null" }

        val code = requestedGroup!!.optionValue
        val name = requestedGroup!!.text
        val semesterNumber = semester

        val filter = Scraper.FilterBuilder()
            .withGroup(code)
            .withSemester(semesterNumber)
            .getFilter()

        val document = scraper.getTimetable(filter)
        val parser = Parser(document, TimetableClass.Type.OnlineBackgroundProvider())
        val timetableDays = parser.getTimetable()
        val semesterStartDate = parser.getSemesterStartDate()

        val semester = Semester(semesterStartDate, semesterNumber)
        val info = Timetable.Info(code, name, semester)
        val timetable = Timetable(timetableDays, info)

        if (saveTimetable()) {
            withContext(Dispatchers.IO) {
                timetableHandler.save(timetable)
            }
        }

        changeProgressBarVisibility(false)

        val intent = Intent(this, TimetableViewActivity::class.java)
        intent.putExtra("timetable", timetable)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun saveTimetable() = follow_checkbox.isChecked

    private fun changeProgressBarVisibility(visible: Boolean) {
        when (visible) {
            true -> progress_bar.visibility = View.VISIBLE
            false -> progress_bar.visibility = View.INVISIBLE
        }
    }
}
