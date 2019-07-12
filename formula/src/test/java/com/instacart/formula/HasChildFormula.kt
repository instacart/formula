package com.instacart.formula

class HasChildFormula<ChildRenderModel>(
    private val child: Formula<Unit, *, Unit, ChildRenderModel>
): Formula<Unit, Int, Unit, HasChildFormula.RenderModel<ChildRenderModel>> {
    class RenderModel<ChildRenderModel>(
        val child: ChildRenderModel
    )

    override fun initialState(input: Unit): Int = 0

    override fun evaluate(
        input: Unit,
        state: Int,
        context: FormulaContext<Int, Unit>
    ): Evaluation<RenderModel<ChildRenderModel>> {
        return Evaluation(
            renderModel = RenderModel(
                child = context.child(child, Unit)
            )
        )
    }
}
