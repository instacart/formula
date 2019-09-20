package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object UsingKeyToScopeCallbacksWithinAnotherFunction {

    class ChildRenderModel(
        val callback: () -> Unit
    )

    class TestRenderModel(
        val first: ChildRenderModel,
        val second: ChildRenderModel
    )

    fun formula() = TestUtils.stateless { context ->
        Evaluation(
            renderModel = TestRenderModel(
                first = context.key("first") {
                    createChild(context)
                },
                second = context.key("second") {
                    createChild(context)
                }
            )
        )
    }

    private fun createChild(context: FormulaContext<Unit>): ChildRenderModel {
        return ChildRenderModel(
            callback = context.callback {
                none()
            }
        )
    }
}
