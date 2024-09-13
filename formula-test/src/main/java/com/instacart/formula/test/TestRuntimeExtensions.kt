package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula
import com.instacart.formula.RuntimeConfig
import com.instacart.formula.plugin.Dispatcher
import com.instacart.formula.plugin.Inspector

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaObserver.input].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    isValidationEnabled: Boolean = true,
    inspector: Inspector? = null,
    dispatcher: Dispatcher? = null,
): TestFormulaObserver<Input, Output, F> {
    val runtimeConfig = RuntimeConfig(
        isValidationEnabled = isValidationEnabled,
        inspector = inspector,
        defaultDispatcher = dispatcher,
    )

    val delegate = RxJavaFormulaTestDelegate(this, runtimeConfig)
    return TestFormulaObserver(delegate)
}

fun <Event> Action<Event>.test() = TestActionObserver(this)

