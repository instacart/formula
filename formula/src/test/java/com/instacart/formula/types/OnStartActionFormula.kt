package com.instacart.formula.types

import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.rxjava3.RxAction
import io.reactivex.rxjava3.core.Observable

/**
 * On first evaluate starts an action which calls a listener.
 */
class OnStartActionFormula(
    private val eventNumber: Int,
) : StatelessFormula<OnStartActionFormula.Input, Unit>() {

    data class Input(
        val onAction: () -> Unit,
    )

    override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                RxAction.fromObservable {
                    val events = 0 until eventNumber
                    for (event in events) {
                        input.onAction()
                    }
                    Observable.empty()
                }.onEvent {
                    none()
                }
            }
        )
    }
}