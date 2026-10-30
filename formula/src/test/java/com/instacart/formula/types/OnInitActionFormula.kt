package com.instacart.formula.types

import com.instacart.formula.Action
import com.instacart.formula.ActionBuilder
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

/**
 * On init starts an action which emits a transition that calls a parent listener.
 */
class OnInitActionFormula(private val eventNumber: Int) : StatelessFormula<OnInitActionFormula.Input, Unit>() {

    data class Input(
        val onAction: () -> Unit,
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        runOnInitAction()
        return Evaluation(
            output = Unit,
        )
    }

    private fun ActionBuilder<Input, Unit>.runOnInitAction() {
        Action.onInit().onEvent {
            transition {
                (0 until eventNumber).forEach {
                    input.onAction()
                }
            }
        }
    }
}
