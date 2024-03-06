package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector
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
        formula: F,
        inspector: Inspector? = null,
        defaultDispatcher: Dispatcher? = null,
    ): TestFormulaObserver<Input, Output, F>

    /**
     * Creates a [TestFormulaObserver]
     */
    fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        input: Input,
        inspector: Inspector? = null,
        dispatcher: Dispatcher? = null,
    ): TestFormulaObserver<Input, Output, F> {
        return test(formula, inspector, dispatcher).input(input)
    }

    fun newRelay(): Relay

    fun streamFormula(): StreamFormulaSubject

    fun <T : Any> emitEvents(events: List<T>): Action<T>

    fun <T : Any> emitEvents(key: Any?, events: List<T>): Action<T>
}

interface Relay {
    fun action(): Action<Unit>
    fun triggerEvent()
}

interface StreamFormulaSubject : IFormula<String, Int> {
    fun emitEvent(event: Int)
}