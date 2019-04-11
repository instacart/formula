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
interface Integration<in Key, Input, RenderModel : Any> {
    fun createFormula(key: Key): Formula<Input, RenderModel>

    fun input(key: Key): Input

    fun init(key: Key): Flowable<RenderModel> {
        return createFormula(key).state(input(key))
    }
}
