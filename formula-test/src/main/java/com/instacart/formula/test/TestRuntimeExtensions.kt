package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.Stream
import io.reactivex.rxjava3.core.Observable

/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * @param input Input passed to [IFormula].
 */
fun <Input : Any, Output : Any, F: IFormula<Input, Output>> F.test(
    input: Input
): TestFormulaObserver<Input, Output, F> {
    return test(Observable.just(input))
}


/**
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 *
 * @param input A stream of inputs passed to [IFormula].
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
 * An extension function to create a [TestFormulaObserver] for a [IFormula] instance.
 */
fun <Output : Any, F: IFormula<Unit, Output>> F.test() = test(Unit)

fun <Message> Stream<Message>.test() = TestStreamObserver(this)
