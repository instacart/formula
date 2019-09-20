package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object SideEffectFormula {

    class RenderModel(
        val triggerSideEffect: () -> Unit
    )

    fun create(onSideEffect: () -> Unit) = TestUtils.create(0) { state, context ->
        Evaluation(
            renderModel = RenderModel(
                triggerSideEffect = context.callback {
                    message(onSideEffect)
                }
            )
        )
    }
}
