package com.example.hwutimetable.scraper

import org.jsoup.nodes.Element

data class Option(val optionValue: String, val text: String) {
    constructor(option: Element) : this(option.`val`(), option.text())
}