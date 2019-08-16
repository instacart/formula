package com.instacart.formula

class StreamCancelFormula : StatelessFormula<Unit, Unit>() {
    var timesCancelledCalled = 0

    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events(Stream.onCancel()) {
                    message {
                        timesCancelledCalled += 1
                    }
                }
            }
        )
    }
}
