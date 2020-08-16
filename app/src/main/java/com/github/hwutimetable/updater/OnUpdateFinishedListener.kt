package com.github.hwutimetable.updater

import com.github.hwutimetable.parser.Timetable

interface OnUpdateFinishedListener {
    /**
     * This method will be invoked by a [UpdatePerformer] object that updates the stored timetables.
     * After the update process has been finished, this method will be invoked and the updated timetables
     * (if any) will be passed as a list.
     */
    fun onUpdateFinished(updated: Collection<Timetable.Info>)
}