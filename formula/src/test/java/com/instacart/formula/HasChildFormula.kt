package com.instacart.formula

class HasChildFormula<ChildOutput, ChildRenderModel>(
    private val child: Formula<Unit, *, ChildOutput, ChildRenderModel>,
    private val onChildOutput: (Int, ChildOutput) -> Transition<Int, ChildOutput> = { _, output ->
        Transition.Factory.output(output)
    }
) : Formula<Unit, Int, ChildOutput, HasChildFormula.RenderModel<ChildRenderModel>> {
    class RenderModel<ChildRenderModel>(
        val state: Int,
        val child: ChildRenderModel
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, ChildOutput>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        return Evaluation(
            renderModel = RenderModel(
                state = state,
                child = context
                    .child(child)
                    .onOutput { onChildOutput(state, it) }
                    .input(Unit)
            )
        )
    }
}
