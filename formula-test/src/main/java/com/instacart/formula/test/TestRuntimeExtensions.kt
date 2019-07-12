package com.instacart.formula.test

import com.instacart.formula.Formula
import io.reactivex.Observable

/**
 * @param input Input passed to [Formula].
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <Input : Any, State, Output, RenderModel, F: Formula<Input, State, Output, RenderModel>> F.test(
    input: Input,
    builder: ChildFormulaRegistryBuilder.() -> Unit = {}
): TestFormulaObserver<Input, Output, RenderModel> {
    val managers = ChildFormulaRegistryBuilder().apply(builder).testManagers
    return TestFormulaObserver(
        testManagers = managers,
        input = Observable.just(input),
        formula = this
    )
}

/**
 * @param builder Enables to set a mock render model for children formulas.
 */
fun <State, Output, RenderModel, F: Formula<Unit, State, Output, RenderModel>> F.test(
    builder: ChildFormulaRegistryBuilder.() -> Unit = {}
) = test(Unit, builder)

