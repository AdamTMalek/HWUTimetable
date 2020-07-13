package com.github.hwutimetable.updater

interface OnUpdateInProgressListener {
    /**
     * Called when the update service has began its work
     */
    fun onUpdateInProgress()
}