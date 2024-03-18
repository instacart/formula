package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.Transition
import com.instacart.formula.test.Relay

class IncrementActionFormula(
    private val incrementRelay: Relay,
    private val executionType: Transition.ExecutionType? = null,
) : Formula<Unit, Int, Int>() {
    override fun initialState(input: Unit): Int {
        return 0
    }

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
        return Evaluation(
            output = state,
            actions = context.actions {
                incrementRelay.action().onEventWithExecutionType(executionType) {
                    transition(state + 1)
                }
            }
        )
    }
}