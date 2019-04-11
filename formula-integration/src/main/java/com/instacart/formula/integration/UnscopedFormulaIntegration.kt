package com.instacart.formula.integration

import com.instacart.formula.Formula
import io.reactivex.Flowable

/**
 * Defines how a [Key] is bounds to a formula.
 *
 * @param Key a backstack entry for this screen.
 * @param Input a formula input
 * @param RenderModel a render model that the formula produces.
 */
abstract class UnscopedFormulaIntegration<in Key, Input, RenderModel : Any> : Integration<Any, Key, RenderModel>() {
    protected abstract fun createFormula(key: Key): Formula<Input, RenderModel>

    protected abstract fun input(key: Key): Input

    override fun create(component: Any, key: Key): Flowable<RenderModel> {
        return createFormula(key).state(input(key))
    }
}
