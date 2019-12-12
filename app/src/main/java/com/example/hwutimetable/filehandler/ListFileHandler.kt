package com.example.hwutimetable.filehandler

import android.content.Context

/**
 * Any class/object that saves and retrieves lists from file
 * should implement this interface.
 */
interface ListFileHandler<T> {
    fun save(member: T)
    fun saveAll(list: List<T>)
    fun getList(): List<T>
    fun delete(member: T)
    fun deleteAll()
}