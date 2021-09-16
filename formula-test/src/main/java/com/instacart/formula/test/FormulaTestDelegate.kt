package com.instacart.formula.test

import com.instacart.formula.IFormula

/**
 * A delegate interface that enables using different runtimes in tests.
 */
interface FormulaTestDelegate<Input, Output, FormulaT : IFormula<Input, Output>> {
    val formula: FormulaT

    fun values(): List<Output>
    fun input(input: Input)
    fun assertNoErrors()
    fun dispose()
}