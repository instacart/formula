package com.instacart.formula

class StreamInputFormula : StatelessFormula<Int, Unit>()  {
    val messages = mutableListOf<Int>()

    override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events("input", Stream.onInput(), input) {
                    message { messages.add(it) }
                }
            }
        )
    }
}
