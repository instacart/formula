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
abstract class Integration<Key, Input, RenderModel : Any> {
    protected abstract fun createFormula(key: Key): Formula<Input, RenderModel>

    protected abstract fun input(key: Key): Input

    fun init(key: Key): Flowable<RenderModel> {
        return createFormula(key).state(input(key))
    }
}
