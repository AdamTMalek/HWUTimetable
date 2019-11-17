package com.example.hwutimetable.parser

import org.jsoup.nodes.Document
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Hash {
    fun compare(a: ByteArray, b: ByteArray) = a.contentEquals(b)

    fun compare(a: Document, b: Document): Boolean {
        return compare(get(a), get(b))
    }

    fun get(document: Document): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        return messageDigest.digest(document.outerHtml().toByteArray(StandardCharsets.UTF_8))
    }
}