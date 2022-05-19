package com.instacart.formula.internal

import com.instacart.formula.IFormula

/**
 * A poor man's thread checker.
 */
class ThreadChecker(private val formula: IFormula<*, *>) {
    private val formulaType = formula::class.qualifiedName
    private val threadName = Thread.currentThread().name
    private val id = Thread.currentThread().id

    fun check(errorMessage: String) {
        val thread = Thread.currentThread()
        if (thread.id != id) {
            throw IllegalStateException("$formulaType - $errorMessage Expected: $threadName, Was: ${thread.name}")
        }
    }
}
