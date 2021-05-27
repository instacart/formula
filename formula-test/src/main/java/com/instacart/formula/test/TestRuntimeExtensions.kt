package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.Stream

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(): TestFormulaObserver<Input, Output, F> {
    return TestFormulaObserver(this)
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

fun <Message> Stream<Message>.test() = TestStreamObserver(this)
