package com.instacart.mvi.integration

import com.instacart.client.mvi.ICViewModel
import io.reactivex.Flowable

/**
 * Defines how an [ICMviFragmentContract] is bound to a [ICViewModel]
 */
interface Integration<Contract, Input, RenderModel> : (Contract) -> Flowable<RenderModel> {
    fun createViewModel(contract: Contract): ICViewModel<Input, RenderModel>

    fun input(contract: Contract): Input

    override fun invoke(p1: Contract): Flowable<RenderModel> {
        return createViewModel(p1).state(input(p1))
    }
}
