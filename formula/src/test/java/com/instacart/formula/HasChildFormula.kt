package com.instacart.formula

class HasChildFormula<ChildInput, ChildRenderModel>(
    private val child: IFormula<ChildInput, ChildRenderModel>,
    private val createChildInput: FormulaContext<Int>.(Int) -> ChildInput
) : Formula<Unit, Int, HasChildFormula.RenderModel<ChildRenderModel>> {
    companion object {
        operator fun <ChildRenderModel> invoke(
            child: IFormula<Unit, ChildRenderModel>
        ): HasChildFormula<Unit, ChildRenderModel> {
            return HasChildFormula(child) { Unit }
        }
    }


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
                child = context.child(child, createChildInput(context, state))
            )
        )
    }
}
