package com.instacart.formula.integration

import com.instacart.formula.Formula
import io.reactivex.Flowable

/**
 * Defines how a [Key] is bounds to a formula.
 *
 * [Key] - a backstack entry for this screen.
 * [Input] - a formula input
 * [RenderModel] - a render model that the formula produces.
 */
interface Integration<Key, Input, RenderModel> : (Key) -> Flowable<RenderModel> {
    fun createViewModel(contract: Key): Formula<Input, RenderModel>

    fun input(key: Key): Input

    override fun invoke(p1: Key): Flowable<RenderModel> {
        return createViewModel(p1).state(input(p1))
    }
}
