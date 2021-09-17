package com.instacart.formula.test

import com.instacart.formula.IFormula
import kotlinx.coroutines.CoroutineScope

/**
 * An extension function to create a [TestFormulaFlow] for a [IFormula] instance.
 *
 * Note: Formula won't start until you pass it an [input][TestFormulaFlow.input].
 */

fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(scope: CoroutineScope): TestFormulaFlow<Input, Output, F> {
    return TestFormulaFlow(scope, this)
}

/**
 * An extension function to create a [TestFormulaFlow] for a [IFormula] instance.
 *
 * @param initialInput Input passed to [IFormula].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    initialInput: Input,
    scope: CoroutineScope
): TestFormulaFlow<Input, Output, F> {
    return test(scope).apply {
        input(initialInput)
    }
}
