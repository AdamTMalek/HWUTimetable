package com.github.hwutimetable

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText

object RenameTimetableDialog {
    fun interface OnRenameCompleteListener {
        fun onRenameComplete(newName: String)
    }

    fun showDialog(activity: Context, currentName: CharSequence, renameListener: OnRenameCompleteListener): Dialog {
        val builder = AlertDialog.Builder(activity)
        val editTextView = LayoutInflater.from(activity).inflate(R.layout.rename_text_edit, null, false)
        val editText = editTextView.findViewById<EditText>(R.id.edit_timetable_title).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            setText(currentName)
        }
        return with(builder) {
            setTitle(activity.getString(R.string.rename_dialog_title))
            setView(editTextView)
            setPositiveButton("Rename") { _, _ -> renameListener.onRenameComplete(editText.text.toString()) }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            show()
        }
    }
}