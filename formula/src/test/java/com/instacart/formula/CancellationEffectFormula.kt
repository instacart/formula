package com.instacart.formula

class CancellationEffectFormula : StatelessFormula<Unit, Unit>() {
    var timesCancelledCalled = 0

    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                cancellationMessage {
                    timesCancelledCalled += 1
                }
            }
        )
    }
}
