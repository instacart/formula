package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector

/**
 * Used to support running tests for various runtimes such as RxJava or Coroutines.
 */
interface TestableRuntime {

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
}

interface Relay {
    fun action(): Action<Unit>
    fun triggerEvent()
}