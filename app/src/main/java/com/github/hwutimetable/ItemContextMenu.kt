package com.github.hwutimetable

import android.app.Activity
import android.app.AlertDialog


/**
 * The [ItemContextMenu] is to be used when an item from the recycler view
 * has been long-pressed.
 * The menu contains entries for  performing different actions on selected
 * timetable.
 */
class ItemContextMenu(private val activity: Activity) {
    /**
     * Represents the chosen action from the context menu
     */
    enum class Action {
        RENAME,
        DELETE,
    }

    /**
     * Called when an action has been selected from the context menu.
     * Does not include the cancel button - this is handled internally
     * and dismisses the dialog.
     */
    fun interface OnActionClickedListener {
        fun onActionClicked(action: Action)
    }

    /**
     * Create the dialog for the context menu with populated options.
     */
    fun create(listener: OnActionClickedListener): AlertDialog {
        return AlertDialog.Builder(activity).run {
            setTitle(R.string.item_context_menu_title)
            setItems(R.array.item_actions) { _, which ->
                when (which) {
                    0 -> listener.onActionClicked(Action.RENAME)
                    1 -> listener.onActionClicked(Action.DELETE)
                }
            }
            setPositiveButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            create()
        }
    }

}
