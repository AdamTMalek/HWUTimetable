package com.github.hwutimetable.validators

import android.widget.EditText
import com.github.hwutimetable.filehandler.TimetableFileHandler


/**
 * The [UniqueTimetableNameValidator] is a validator that ensures the widget
 * that is an input for the timetable name will be validated, to ensure the
 * name is not already used by another timetable.
 *
 * @param fileHandler [TimetableFileHandler] from which the taken names will be fetched.
 */
class UniqueTimetableNameValidator(fileHandler: TimetableFileHandler) : EditTextValidator {
    private val timetableNames by lazy {
        fileHandler.getStoredTimetables().map { it.name }
    }

    override val errorString: String
        get() = "This name is already used."

    override fun validate(widget: EditText): Boolean {
        return widget.text.toString() !in timetableNames
    }
}
