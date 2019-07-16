package com.instacart.formula

class OptionalChildFormula<ChildOutput, ChildRenderModel>(
    private val child: Formula<Unit, *, ChildOutput, ChildRenderModel>,
    private val onChildOutput: (State, ChildOutput) -> Transition<State, ChildOutput> = { _, output -> Transition.Factory.output(output) }
): Formula<Unit, OptionalChildFormula.State, ChildOutput, OptionalChildFormula.RenderModel<ChildRenderModel>> {

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
        context: FormulaContext<State, ChildOutput>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        val childRM = if (state.showChild) {
            context.child(child, Unit) {
                onChildOutput(state, it)
            }
        } else {
            null
        }

        return Evaluation(
            renderModel = RenderModel(
                child = childRM,
                toggleChild = context.callback("toggle") {
                    state.copy(showChild = !state.showChild).transition()
                }
            )
        )
    }
}
