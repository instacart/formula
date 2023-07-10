package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.test.TestableRuntime
import com.instacart.formula.types.IncrementFormula
import io.reactivex.rxjava3.core.Observable

class ParentUpdateChildAndSelfOnEventRobot(
    runtime: TestableRuntime,
) {

    val subject = runtime.test(Parent())

    fun start() = apply {
        subject.input(Unit)
    }

    class Parent : Formula<Unit, Parent.State, Parent.Output>() {
        data class State(
            val actionId: Int = 0,
            val value: Int = 0,
        )

        data class Output(
            val parentValue: Int,
            val childValue: Int,
            val onAction: () -> Unit
        )

        private val incrementFormula = IncrementFormula()

        override fun initialState(input: Unit): State = State()

        override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
            val incrementOutput = context.child(incrementFormula)
            return Evaluation(
                output = Output(
                    parentValue = state.value,
                    childValue = incrementOutput.value,
                    onAction = context.callback {
                        val newState = state.copy(actionId = state.actionId + 1)
                        transition(newState)
                    }
                ),
                actions = context.actions {
                    if (state.actionId > 0) {
                        RxAction.fromObservable(state.actionId) {
                            // Call child
                            incrementOutput.onIncrement()

                            // Emit events
                            Observable.just(1, 1, 1)
                        }.onEvent {
                            val newState = state.copy(value = state.value + it)
                            transition(newState)
                        }
                    }
                }
            )
        }
    }
}