package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object OptionalChildFormula {
    data class State(
        val showChild: Boolean = true
    )

    class RenderModel<ChildRenderModel>(
        val child: ChildRenderModel?,
        val toggleChild: () -> Unit
    )

    fun <ChildInput, ChildRenderModel> create(
        child: Formula<ChildInput, *, ChildRenderModel>,
        childInput: FormulaContext<State>.(State) -> ChildInput
    ): Formula<Unit, State, RenderModel<ChildRenderModel>> {
        return TestUtils.create(State()) { state, context ->
            val childRM = if (state.showChild) {
                context.child(child).input { childInput(context, state) }
            } else {
                null
            }

            Evaluation(
                renderModel = RenderModel(
                    child = childRM,
                    toggleChild = context.callback {
                        state.copy(showChild = !state.showChild).noMessages()
                    }
                )
            )
        }
    }

    fun <ChildRenderModel> create(
        child: Formula<Unit, *, ChildRenderModel>
    ): Formula<Unit, State, RenderModel<ChildRenderModel>> {
        return create(child) { Unit }
    }
}
