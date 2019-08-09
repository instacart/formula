package com.instacart.formula

class HasChildFormulaV2<ChildInput, ChildRenderModel>(
    private val child: Formula<ChildInput, *, ChildRenderModel>,
    private val createChildInput: FormulaContext<Int>.(Int) -> ChildInput
) : Formula<Unit, Int, HasChildFormulaV2.RenderModel<ChildRenderModel>> {

    class RenderModel<ChildRenderModel>(
        val state: Int,
        val child: ChildRenderModel
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        return Evaluation(
            renderModel = RenderModel(
                state = state,
                child = context
                    .child(child)
                    .input(createChildInput(context, state))
            )
        )
    }
}
