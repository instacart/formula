package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.rxjava3.RxAction
import io.reactivex.rxjava3.core.Observable

object ChildActionFiresParentEventOnStart {

    fun formula(runChildOnStart: Boolean, values: List<Int>) = Parent(Child(values), runChildOnStart)

    class Parent(val child: Child, val runChildOnStart: Boolean) : Formula<Unit, Parent.State, Parent.Output>() {
        data class State(
            val runChild: Boolean = false,
            val value: Int = 0,
        )

        data class Output(
            val value: Int,
            val showChild: (Boolean) -> Unit
        )

        override fun initialState(input: Unit): State = State(
            runChild = runChildOnStart,
        )

        override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
            if (state.runChild) {
                val listener = context.onEvent<Int> {
                    val newState = state.copy(value = state.value + it)
                    transition(newState)
                }
                context.child(child, listener)
            }

            return Evaluation(
                output = Output(
                    value = state.value,
                    showChild = context.onEvent {
                        val newState = state.copy(runChild = it)
                        transition(newState)
                    }
                ),
            )
        }
    }

    class Child(val values: List<Int>) : StatelessFormula<Listener<Int>, Unit>() {
        override fun Snapshot<Listener<Int>, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                actions = context.actions {
                    RxAction.fromObservable {
                        for (value in values) {
                            input(value)
                        }
                        Observable.empty()
                    }.onEvent { none() }
                }
            )
        }
    }
}