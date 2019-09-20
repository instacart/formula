package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object HasChildFormula {
    class RenderModel<ChildRenderModel>(
        val state: Int,
        val child: ChildRenderModel
    )

    fun <ChildRenderModel> create(
        child: Formula<Unit, *, ChildRenderModel>
    ) = create(child) { Unit }

    fun <ChildInput, ChildRenderModel> create(
        child: Formula<ChildInput, *, ChildRenderModel>,
        createChildInput: FormulaContext<Int>.(Int) -> ChildInput
    ) = TestUtils.create(0) { state, context ->
        Evaluation(
            renderModel = RenderModel(
                state = state,
                child = context
                    .child(child)
                    .input(createChildInput(context, state))
            )
        )
    }
}
