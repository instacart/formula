package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Transition
import com.instacart.formula.test.Relay

class IncrementActionFormula(
    private val incrementRelay: Relay,
    private val executionType: Transition.ExecutionType? = null,
) : StatelessFormula<Unit, Int>() {

    private val actionInput = ActionDelegateFormula.Input(
        delegateAction = {
            if (executionType == null) {
                incrementRelay.action().onEvent {
                    transition(state + 1)
                }
            } else {
                incrementRelay.action().onEventWithExecutionType(executionType) {
                    transition(state + 1)
                }
            }
        },
        onAction = {}
    )

    private val actionFormula = ActionDelegateFormula()

    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
        return Evaluation(
            output = context.child(actionFormula, actionInput)
        )
    }
}