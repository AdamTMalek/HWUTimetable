package com.github.hwutimetable.parser

class Clashes {
    private val clashes = mutableListOf<Clash>()

    fun addClash(clash: Clash) {
        clashes.add(clash)
    }

    fun addClashes(clashes: Clashes) {
        this.clashes.addAll(clashes.getClashes())
    }

    fun getClashes(): List<Clash> {
        return clashes
    }

    fun getClashes(day: Day): List<Clash> {
        return clashes.filter { clash -> clash.day == day }
    }

    fun isEmpty(): Boolean {
        return clashes.isEmpty()
    }
}