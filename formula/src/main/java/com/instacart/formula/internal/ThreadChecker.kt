package com.instacart.formula.internal

/**
 * A poor man's thread checker.
 */
class ThreadChecker {
    private val threadName = Thread.currentThread().name
    private val id = Thread.currentThread().id

    fun check(errorMessage: String) {
        val thread = Thread.currentThread()
        if (thread.id != id) {
            throw IllegalStateException("$errorMessage Expected: $threadName, Was: ${thread.name}")
        }
    }
}
