package com.instacart.formula

class HasChildFormula<ChildInput, ChildRenderModel>(
    private val child: Formula<ChildInput, *, ChildRenderModel>
) : Formula<ChildInput, Int, HasChildFormula.RenderModel<ChildRenderModel>> {

    class RenderModel<ChildRenderModel>(
        val state: Int,
        val child: ChildRenderModel
    )

    override fun initialState(input: ChildInput): Int = 0

    override fun evaluate(
        input: ChildInput,
        state: Int,
        context: FormulaContext<Int>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        return Evaluation(
            renderModel = RenderModel(
                state = state,
                child = context.child(child).input(input)
            )
        )
    }
}
