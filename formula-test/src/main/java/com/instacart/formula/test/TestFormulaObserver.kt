package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.rxjava3.toObservable
import io.reactivex.rxjava3.core.Observable

class TestFormulaObserver<Input : Any, Output : Any, FormulaT : IFormula<Input, Output>>(
    private val input: Observable<Input>,
    val formula: FormulaT
) {

    private val observer = formula
        .toObservable(input)
        .test()
        .assertNoErrors()

    fun values(): List<Output> {
        return observer.values()
    }

    inline fun output(assert: Output.() -> Unit) = apply {
        assert(values().last())
    }

    fun assertOutputCount(count: Int) = apply {
        val size = values().size
        assert(size == count) {
            "Expected: $count, was: $size"
        }
    }

    fun dispose() = apply {
        observer.dispose()
    }
}
