package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.Stream
import io.reactivex.rxjava3.core.Observable

/**
 * @param input Input passed to [IFormula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    input: Input
): TestFormulaObserver<Input, Output, F> {
    return test(Observable.just(input))
}


/**
 * @param input A stream of inputs passed to [Formula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    input: Observable<Input>
): TestFormulaObserver<Input, Output, F> {
    return TestFormulaObserver(
        input = input,
        formula = this
    )
}

/**
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Output : Any, F: IFormula<Unit, Output>> F.test() = test(Unit)

fun <Message> Stream<Message>.test() = TestStreamObserver(this)
