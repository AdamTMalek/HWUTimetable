package com.github.hwutimetable

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.InputType
import android.widget.EditText

object RenameTimetableDialog {
    fun interface OnRenameCompleteListener {
        fun onRenameComplete(newName: String)
    }

    fun showDialog(activity: Context, currentName: CharSequence, renameListener: OnRenameCompleteListener): Dialog {
        val builder = AlertDialog.Builder(activity)
        val editText = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            setText(currentName)
        }
        return with(builder) {
            setView(editText)
            setPositiveButton("Rename") { _, _ -> renameListener.onRenameComplete(editText.text.toString()) }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            show()
        }
    }
}