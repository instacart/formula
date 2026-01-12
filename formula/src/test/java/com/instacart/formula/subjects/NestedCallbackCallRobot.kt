package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import kotlinx.coroutines.CoroutineScope

import com.instacart.formula.test.test

class NestedCallbackCallRobot(scope: CoroutineScope) {
    val subject = Parent().test(scope)

    fun start() = apply {
        subject.input(Unit)
    }

    class Parent : Formula<Unit, Int, Parent.Output>() {
        data class Output(
            val value: Int,
            val onAction: () -> Unit,
        )

        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Output> {
            val next = context.callback {
                transition(state + 1)
            }
            return Evaluation(
                output = Output(
                    value = state,
                    onAction = context.callback {
                        transition {
                            next()
                        }
                    }
                )
            )
        }
    }
}