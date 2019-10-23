package com.instacart.formula

class NestedKeyFormula : Formula<Unit, Unit, NestedKeyFormula.RenderModel> {

    class RenderModel(val callback: () -> Unit)

    override fun initialState(input: Unit) = Unit

    override fun evaluate(
        input: Unit,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<RenderModel> {

        val callback = context.key("first level") {
            context.key("second level") {
                context.callback {
                    none()
                }
            }
        }

        return Evaluation(
            renderModel = RenderModel(callback)
        )
    }
}
