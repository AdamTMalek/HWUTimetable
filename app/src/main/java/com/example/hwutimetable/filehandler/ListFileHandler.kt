package com.example.hwutimetable.filehandler

import android.content.Context

/**
 * Any class/object that saves and retrieves lists from file
 * should implement this interface.
 */
interface ListFileHandler<T> {
    fun save(context: Context, member: T)
    fun saveAll(context: Context, list: List<T>)
    fun getList(context: Context): List<T>
    fun delete(context: Context, member: T)
    fun deleteAll(context: Context)
}