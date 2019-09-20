package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object EventCallbackFormula {

    data class RenderModel(
        val state: String,
        val changeState: (String) -> Unit
    )

    fun create() = TestUtils.create("") { state, context ->
        Evaluation(
            renderModel = RenderModel(
                state = state,
                changeState = context.eventCallback { newState ->
                    newState.noMessages()
                }
            )
        )
    }
}
