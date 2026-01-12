package com.instacart.formula.test

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot

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

    // TODO: enabling validation is a breaking behavior.
    val observer = formula.testUnscoped(isValidationEnabled = false)
    return try {
        observer.input(input)
        observer.assertNoErrors()
        observer.values().last()
    } finally {
        observer.dispose()
    }
}

/**
 *
 * Creates a test formula for a specific [IFormula] instance.
 *
 * ```kotlin
 * class FakeUserFormula : UserFormula {
 *      override val implementation = testFormula(
 *          initialOutput = User(
 *              name = "Fake user name",
 *          )
 *      )
 * }
 * ```
 */
fun <Input, Output> IFormula<Input, Output>.testFormula(
    initialOutput: Output,
): TestFormula<Input, Output> {
    return TestFormula(this, initialOutput)
}