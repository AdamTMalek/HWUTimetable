package com.example.hwutimetable.parser

import android.content.Context
import com.example.hwutimetable.R

/**
 * Item type represents a type of an timetable item.
 * This can be a lecture, tutorial, lab etc.
 * @property name: Type as it appears on the website timetable (e.g. CLab, Tut, Lec)
 */
class ItemType(val name: String) {

    /**
     * Get the background color associated with this type of item
     */
    fun getColor(context: Context): Int {
        val id = getId(context)
        return context.resources.getColor(id, context.theme)
    }

    private fun getId(context: Context): Int {
        val name = "item_${name.toLowerCase()}"
        val id = context.resources.getIdentifier(name, "color", context.packageName)

        if (id == 0) {
            // Color with this name was not found. Get the default.
            return context.resources.getColor(R.color.item_default, context.theme)
        }

        return id
    }
}