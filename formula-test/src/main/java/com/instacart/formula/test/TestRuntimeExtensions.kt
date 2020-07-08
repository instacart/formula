package com.instacart.formula.test

import com.instacart.formula.IFormula
import com.instacart.formula.Stream
import io.reactivex.rxjava3.core.Observable

/**
 * @param input Input passed to [IFormula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, RenderModel : Any, F: IFormula<Input, RenderModel>> F.test(
    input: Input
): TestFormulaObserver<Input, RenderModel, F> {
    return test(Observable.just(input))
}


/**
 * @param input A stream of inputs passed to [Formula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, RenderModel : Any, F: IFormula<Input, RenderModel>> F.test(
    input: Observable<Input>
): TestFormulaObserver<Input, RenderModel, F> {
    return TestFormulaObserver(
        input = input,
        formula = this
    )
}

/**
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <RenderModel : Any, F: IFormula<Unit, RenderModel>> F.test() = test(Unit)

fun <Message> Stream<Message>.test() = TestStreamObserver(this)
