package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.Stream
import org.junit.rules.TestRule

/**
 * Used to support running tests for various runtimes such as RxJava or Coroutines.
 */
interface TestableRuntime {
    val rule: TestRule

    /**
     * Creates a [TestFormulaObserver]
     */
    fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F
    ): TestFormulaObserver<Input, Output, F>

    /**
     * Creates a [TestFormulaObserver]
     */
    fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        input: Input,
    ): TestFormulaObserver<Input, Output, F> {
        return test(formula).input(input)
    }

    fun newRelay(): Relay

    fun streamFormula(): StreamFormulaSubject

    fun <T> emitEvents(events: List<T>): Stream<T>
}

interface Relay {
    fun stream(): Stream<Unit>
    fun triggerEvent()
}

interface StreamFormulaSubject : IFormula<String, Int> {
    fun emitEvent(event: Int)
}