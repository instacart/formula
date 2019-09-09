package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.Stream
import io.reactivex.Observable

/**
 * @param input Input passed to [Formula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, State, RenderModel : Any, F: Formula<Input, State, RenderModel>> F.test(
    input: Input,
    builder: ChildFormulaRegistryBuilder.() -> Unit = {}
): TestFormulaObserver<Input, RenderModel, F> {
    return test(Observable.just(input), builder)
}


/**
 * @param input A stream of inputs passed to [Formula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, State, RenderModel : Any, F: Formula<Input, State, RenderModel>> F.test(
    input: Observable<Input>,
    builder: ChildFormulaRegistryBuilder.() -> Unit = {}
): TestFormulaObserver<Input, RenderModel, F> {
    val managers = ChildFormulaRegistryBuilder().apply(builder).testManagers
    return TestFormulaObserver(
        testManagers = managers,
        input = input,
        formula = this
    )
}

/**
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <State, RenderModel : Any, F: Formula<Unit, State, RenderModel>> F.test(
    builder: ChildFormulaRegistryBuilder.() -> Unit = {}
) = test(Unit, builder)



fun <Message> Stream<Message>.test() = TestStreamObserver(this)
