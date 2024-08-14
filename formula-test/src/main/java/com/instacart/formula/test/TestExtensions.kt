package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.toObservable

/**
 * A utility to help test functions that require [FormulaContext] or [Snapshot].
 */
fun <State : Any, Output : Any> withSnapshot(
    state: State,
    output: Snapshot<Unit, State>.() -> Output
): Output {
    return withSnapshot(Unit, state, output)
}

/**
 * A utility to help test functions that require [FormulaContext] or [Snapshot].
 */
fun <Input : Any, State : Any, Output : Any> withSnapshot(
    input: Input,
    state: State,
    output: Snapshot<Input, State>.() -> Output
): Output {
    val formula = object : Formula<Input, State, Output>() {
        override fun initialState(input: Input): State {
            return state
        }

        override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
            return Evaluation(output = output())
        }
    }
    val observer = formula.toObservable(input).test()
    observer.assertNoErrors()
    observer.dispose()
    return observer.values().last()
}

fun <Input, Output> IFormula<Input, Output>.testFormula(
    initialOutput: Output,
    key: (Input) -> Any? = { null }
): TestFormula<Input, Output> {
    return TestFormula(initialOutput, key)
}