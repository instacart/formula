package com.instacart.formula.integration

import com.instacart.formula.Formula
import io.reactivex.Flowable

/**
 * Defines how an [ICMviFragmentContract] is bound to a [Formula]
 */
interface Integration<Contract, Input, RenderModel> : (Contract) -> Flowable<RenderModel> {
    fun createViewModel(contract: Contract): Formula<Input, RenderModel>

    fun input(contract: Contract): Input

    override fun invoke(p1: Contract): Flowable<RenderModel> {
        return createViewModel(p1).state(input(p1))
    }
}
