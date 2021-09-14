package com.instacart.formula

class NestedKeyFormula : Formula<Unit, Unit, NestedKeyFormula.Output> {

    class Output(val callback: () -> Unit)

    override fun initialState(input: Unit) = Unit

    override fun evaluate(
        input: Unit,
        state: Unit,
        context: FormulaContext<Unit>
    ): Evaluation<Output> {

        val callback = context.key("first level") {
            context.key("second level") {
                context.onEvent {
                    none()
                }
            }
        }

        return Evaluation(
            output = Output(callback)
        )
    }
}
