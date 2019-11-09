package com.example.hwutimetable

import kotlinx.serialization.Serializable

@Serializable
data class TimetableInfo(val code: String, val name: String)