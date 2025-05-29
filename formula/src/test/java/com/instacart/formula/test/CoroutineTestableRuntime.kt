package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Inspector
import com.instacart.formula.plugin.Dispatcher

object CoroutinesTestableRuntime : TestableRuntime {

    override fun <Input : Any, Output : Any, F : IFormula<Input, Output>> test(
        formula: F,
        inspector: Inspector?,
        defaultDispatcher: Dispatcher?,
        isValidationEnabled: Boolean,
    ): TestFormulaObserver<Input, Output, F> {
        val runtimeConfig = RuntimeConfig(
            isValidationEnabled = isValidationEnabled,
            inspector = inspector,
            defaultDispatcher = defaultDispatcher
        )
        val delegate = CoroutineTestDelegate(formula, runtimeConfig)
        return TestFormulaObserver(delegate)
    }

    override fun newRelay(): Relay {
        return FlowRelay()
    }
}
