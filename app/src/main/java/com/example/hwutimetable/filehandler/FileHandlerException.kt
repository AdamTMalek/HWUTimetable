package com.example.hwutimetable.filehandler

import java.lang.Exception


class FileHandlerException(message: String, val reason: Reason) : Exception(message) {
    enum class Reason {
        NOT_FOUND,
        CORRUPTED
    }
}