package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula

object ParentTransitionOnChildActionStart {

    fun formula() = Parent(Child())

    class Parent(val child: Child) : Formula<Unit, Int, Int>() {
        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            val input = Child.Input(
                onIncrementState = context.callback {
                    transition(state + 1)
                }
            )
            context.child(child, input)
            return Evaluation(
                output = state,
            )
        }
    }

    class Child : StatelessFormula<Child.Input, Unit>() {
        data class Input(
            val onIncrementState: () -> Unit,
        )

        override fun Snapshot<Input, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                actions = context.actions {
                    Action.onInit().onEvent {
                        transition {
                            input.onIncrementState()
                            input.onIncrementState()
                            input.onIncrementState()
                        }
                    }
                }
            )
        }
    }
}