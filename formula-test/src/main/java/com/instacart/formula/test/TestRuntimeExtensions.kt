package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.FormulaRuntime
import io.reactivex.Observable
import io.reactivex.observers.TestObserver

fun <Input, State, Output, RenderModel, F: Formula<Input, State, Output, RenderModel>> F.test(
    input: Input
): TestObserver<RenderModel> {
    return FormulaRuntime.start(Observable.just(input), this, {}).test()
}
