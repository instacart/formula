package com.instacart.formula

class OptionalChildFormula<ChildInput, ChildRenderModel>(
    private val child: IFormula<ChildInput, ChildRenderModel>,
    private val childInput: FormulaContext<State>.(State) -> ChildInput
): Formula<Unit, OptionalChildFormula.State, OptionalChildFormula.RenderModel<ChildRenderModel>> {
    companion object {
        operator fun <ChildRenderModel> invoke(child: IFormula<Unit, ChildRenderModel>) = run {
            OptionalChildFormula(child) { Unit }
        }
    }

    data class State(
        val showChild: Boolean = true
    )

    class RenderModel<ChildRenderModel>(
        val child: ChildRenderModel?,
        val toggleChild: () -> Unit
    )

    override fun initialState(input: Unit) = State()

    override fun evaluate(
        input: Unit,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        val childRM = if (state.showChild) {
            context.child(child, childInput(context, state))
        } else {
            null
        }

        return Evaluation(
            renderModel = RenderModel(
                child = childRM,
                toggleChild = context.callback {
                    state.copy(showChild = !state.showChild).noEffects()
                }
            )
        )
    }
}
