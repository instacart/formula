package com.instacart.formula

class TerminateFormula : StatelessFormula<Unit, Unit>() {
    var timesTerminateCalled = 0

    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
        return Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events(Stream.onTerminate()) {
                    transition { timesTerminateCalled += 1 }
                }
            }
        )
    }
}
