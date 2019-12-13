package com.example.hwutimetable.parser

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Item type represents a type of an timetable item.
 * This can be a lecture, tutorial, lab etc.
 * @property name: Type as it appears on the website timetable (e.g. CLab, Tut, Lec)
 */
class ItemType(val name: String) {

    /**
     * Gets the background associated with this item type
     * @return: A background from drawable resources
     */
    fun getBackground(context: Context): Drawable {
        val id = getId(context)
        return ContextCompat.getDrawable(context, id)
            ?: throw Resources.NotFoundException("Failed to load drawable with id $id")
    }

    /**
     * Gets the id of the drawable
     */
    private fun getId(context: Context): Int {
        val name = this.name.toLowerCase()
        val typeName = when(name) {
            "wkp", "sgrp", "plab", "llab" -> "lab"  // These have the same background
            else -> name
        }.plus("_background") // add _background suffix

        val id = context.resources.getIdentifier(typeName, "drawable", context.packageName)

        if (id == 0) {
            throw Resources.NotFoundException("The background with name $name was not found in the resources")
        }

        return id
    }

    override fun toString(): String {
        return name
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemType

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}