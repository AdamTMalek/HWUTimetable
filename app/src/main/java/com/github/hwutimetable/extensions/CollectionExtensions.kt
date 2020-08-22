package com.github.hwutimetable.extensions

/**
 * Clears the collection from existing elements and then adds all [elements].
 */
fun <E> MutableCollection<E>.clearAndAddAll(elements: Collection<E>) {
    this.clear()
    this.addAll(elements)
}
