package com.instacart.formula.test

import com.instacart.formula.Action
import com.instacart.formula.IFormula

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaObserver.input].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    isValidationEnabled: Boolean = true,
): TestFormulaObserver<Input, Output, F> {
    return TestFormulaObserver(RxJavaFormulaTestDelegate(this, isValidationEnabled))
}

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * @param initialInput Input passed to [IFormula].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    initialInput: Input
): TestFormulaObserver<Input, Output, F> {
    return test().apply {
        input(initialInput)
    }
}

fun <Event> Action<Event>.test() = TestActionObserver(this)

