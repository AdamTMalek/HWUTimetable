package com.github.hwutimetable

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.github.hwutimetable.extensions.clearAndAddAll
import com.github.hwutimetable.filehandler.TimetableFileHandler
import com.github.hwutimetable.parser.Timetable
import com.github.hwutimetable.scraper.Option
import com.github.hwutimetable.scraper.TimetableScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The [AddTimetableActivity] is an abstract class that provides the majority of methods
 * for activities that are used for adding a course or a programme timetable.
 *
 * Since scraping for course and programme timetable differs, different scrapers need to
 * be used. Hence why, the [AddTimetableActivity] needs to have a specified [ScraperType].
 *
 * Note that because the [AddTimetableActivity] uses Hilt and Dependency Injection, all
 * classes inheriting from this class, must be annotated with @AndroidEntryPoint annotation
 * for Hilt to work.
 */
abstract class AddTimetableActivity<ScraperType : TimetableScraper, ViewBindingType : ViewBinding> :
    AppCompatActivity(),
    AdapterView.OnItemSelectedListener {
    @Inject
    lateinit var scraper: ScraperType

    @Inject
    lateinit var timetableHandler: TimetableFileHandler

    @Inject
    lateinit var currentDateProvider: CurrentDateProvider

    /**
     * [mainScope] is a coroutine scope used for any network calls/scraping.
     */
    protected val mainScope = MainScope()

    /**
     * List of department options that scraper has fetched
     */
    protected val departmentsOptions = mutableListOf<Option>()

    /**
     * List of group options that scraper has fetched
     */
    protected val groupOptions = mutableSetOf<Option>()

    /**
     * View binding
     */
    protected lateinit var viewBinding: ViewBindingType

    protected val getTimetable: Button by lazy { findViewById<Button>(R.id.get_timetable) }
    protected val departmentsSpinner: Spinner by lazy { findViewById<Spinner>(R.id.departments_spinner) }
    protected val semesterSpinner: Spinner by lazy { findViewById<Spinner>(R.id.semester_spinner) }
    protected val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private val getTimetableButton: Button by lazy { findViewById<Button>(R.id.get_timetable) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewBinding()
        setContentView(viewBinding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setupView()

        setSpinnerSelectionListener()
        setClosestSemester()
        setGetTimetableButtonClickHandler()
        setGroupInputChangeListener()

        mainScope.launch {
            populateSpinners()
        }
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.INVISIBLE
    }

    private fun setViewBinding() {
        viewBinding = inflateViewBinding()
        setContentView(viewBinding.root)
    }

    protected abstract fun inflateViewBinding(): ViewBindingType

    private fun setGetTimetableButtonClickHandler() {
        getTimetableButton.setOnClickListener { onGetTimetableButtonClick() }
    }

    private fun setGroupInputChangeListener() {
        findViewById<AutoCompleteTextView>(R.id.groups_input).setOnItemClickListener { _, _, _, _ ->
            findViewById<Button>(R.id.get_timetable).isEnabled = true
            KeyboardManager.hideKeyboard(this)
        }

        findViewById<AutoCompleteTextView>(R.id.groups_input).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val inputString = s?.toString() ?: return
                onGroupValidated(groupOptions.any { it.text == inputString })
            }
        })
    }

    protected abstract fun onGroupValidated(valid: Boolean)

    protected abstract fun onGetTimetableButtonClick()

    /**
     * This method is called in [onCreate].
     * [setupView] must set the content view (using [setContentView]).
     * Any additional view related settings, like setting the title, can go in here.
     */
    protected abstract fun setupView()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed(); true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Sets the [AdapterView.OnItemSelectedListener] on the spinners.
     */
    protected open fun setSpinnerSelectionListener() {
        departmentsSpinner.onItemSelectedListener = this
        semesterSpinner.onItemSelectedListener = this
    }

    /**
     * Populates the spinners of this activity with the options scraped from the website.
     * By default, this only populates the departments options.
     */
    protected open suspend fun populateSpinners() {
        changeProgressBarVisibility(true)

        scraper.setup()
        departmentsOptions.addAll(scraper.getDepartments())
        populateSpinner(findViewById(R.id.departments_spinner), departmentsOptions.map { it.text })

        changeProgressBarVisibility(false)
    }

    /**
     * Populates the given [spinner] with the given list of [elements].
     */
    protected fun populateSpinner(spinner: Spinner, elements: List<String>) {
        spinner.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, elements)
    }

    /**
     * Gets the groups from the website, with the [filters] applied.
     */
    protected suspend fun populateGroups(filters: Map<String, Any>) {
        groupOptions.clearAndAddAll(scraper.getGroups(filters))

        if (isSemesterFilterApplied())
            filterGroupsBySemester()

        populateGroupsInput()
    }

    /**
     * Filters [groupOptions] by semester. This has to modify the [groupOptions]
     */
    protected abstract fun filterGroupsBySemester()

    /**
     * Populates a view with the elements in [groupOptions]
     */
    private fun populateGroupsInput() {
        findViewById<AutoCompleteTextView>(R.id.groups_input).setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                groupOptions.map { it.text })
        )
    }

    /**
     * Changes the visibility of the [progress_bar] depending on the passed value.
     * @param visible If `true`, the progress bar will be visible.
     */
    protected fun changeProgressBarVisibility(visible: Boolean) {
        progressBar.visibility = when (visible) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
    }

    /**
     * Uses the current date to automatically set the closest semester in the semester spinner.
     */
    private fun setClosestSemester() {
        findViewById<Spinner>(R.id.semester_spinner).setSelection(getClosestSemester() - 1)
    }

    protected fun getClosestSemester(): Int {
        val currentDate = currentDateProvider.getCurrentDate()
        return if (currentDate.monthOfYear >= 6)
            1
        else
            2
    }

    /**
     * Uses the semester spinner to determine if to apply the semester filter.
     *
     * @return `true` if the semester filter is to be applied, `false` otherwise.
     */
    private fun isSemesterFilterApplied() =
        semesterSpinner.selectedItem as String != getString(R.string.semester_any)

    /**
     * Uses the "save" checkbox to determine if the timetable is to be saved.
     *
     * @return `true` if the timetable is to be saved, `false` otherwise.
     */
    protected fun isSaveTimetableChecked() = findViewById<CheckBox>(R.id.save_checkbox).isChecked

    protected suspend fun saveTimetable(timetable: Timetable) {
        withContext(Dispatchers.IO) {
            timetableHandler.save(timetable)
        }
    }

    protected fun startViewTimetableActivity(timetable: Timetable) {
        val intent = Intent(this, TimetableViewActivity::class.java)
        intent.putExtra("timetable", timetable)
        // Calling finish will remove the activity from the history stack.
        // This means that a return View Timetable activity will go to the main menu, and not an Add Timetable activity.
        finish()
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // This is a required override by the AdapterView.OnItemSelectedListener
        // but we don't do anything in here.
    }
}