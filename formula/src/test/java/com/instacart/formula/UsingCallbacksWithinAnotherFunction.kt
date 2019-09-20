package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object UsingCallbacksWithinAnotherFunction {

    fun test() = formula().start(Unit).test()

    fun formula() = TestUtils.stateless { context ->
        Evaluation(
            renderModel = TestRenderModel(
                first = createDefaultCallback(context),
                second = createDefaultCallback(context)
            )
        )
    }

    private fun createDefaultCallback(context: FormulaContext<Unit>): () -> Unit {
        return context.callback {
            none()
        }
    }

    class TestRenderModel(
        val first: () -> Unit,
        val second: () -> Unit
    )
}
