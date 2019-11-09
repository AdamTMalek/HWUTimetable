package com.example.hwutimetable.parser

import java.lang.Exception

/**
 * This exception class is used to represent any exception that occur during
 * parsing that cannot be represented with any other built-in exception classes.
 */
class ParserException(message: String?) : Exception(message)