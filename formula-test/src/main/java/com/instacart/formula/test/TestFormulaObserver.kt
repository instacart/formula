package com.instacart.formula.test

import com.instacart.formula.Formula
import com.instacart.formula.FormulaRuntime
import io.reactivex.rxjava3.core.Observable

class TestFormulaObserver<Input : Any, RenderModel : Any, FormulaT : Formula<Input, *, RenderModel>>(
    private val input: Observable<Input>,
    val formula: FormulaT
) {

    private val observer = FormulaRuntime
        .start(
            input = input,
            formula = formula
        )
        .test()
        .assertNoErrors()

    fun values(): List<RenderModel> {
        return observer.values()
    }

    inline fun renderModel(assert: RenderModel.() -> Unit) = apply {
        assert(values().last())
    }

    fun assertRenderModelCount(count: Int) = apply {
        val size = values().size
        assert(size == count) {
            "Expected: $count, was: $size"
        }
    }

    fun dispose() = apply {
        observer.dispose()
    }
}
