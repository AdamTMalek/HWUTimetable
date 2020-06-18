package com.example.hwutimetable.updater

import com.example.hwutimetable.filehandler.TimetableInfo

interface OnUpdateFinishedListener {
    /**
     * This method will be invoked by a [UpdatePerformer] object that updates the stored timetables.
     * After the update process has been finished, this method will be invoked and the updated timetables
     * (if any) will be passed as a list.
     */
    fun onUpdateFinished(updated: Collection<TimetableInfo>)
}