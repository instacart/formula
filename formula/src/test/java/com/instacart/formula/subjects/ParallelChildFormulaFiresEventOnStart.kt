package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.rxjava3.RxAction
import io.reactivex.rxjava3.core.Observable

object ParallelChildFormulaFiresEventOnStart {


    fun formula(events: List<Unit>): Parent {
        return Parent(
            first = FirstChild(),
            second = SecondChild(events),
        )
    }


    class FirstChild : Formula<Unit, Int, FirstChild.Output>() {
        data class Output(
            val value: Int,
            val onIncrement: Listener<Unit>,
        )

        override fun initialState(input: Unit): Int {
            return 0
        }

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    value = state,
                    onIncrement = context.onEvent {
                        transition(state + 1)
                    },
                )
            )
        }
    }

    class SecondChild(val events: List<Unit>) : StatelessFormula<Listener<Unit>, Unit>() {
        override fun Snapshot<Listener<Unit>, Unit>.evaluate(): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                actions = context.actions {
                    RxAction.fromObservable {
                        for (event in events) {
                            input(Unit)
                        }
                        Observable.empty()
                    }.onEvent {
                        none()
                    }
                }
            )
        }
    }

    class Parent(
        val first: FirstChild,
        val second: SecondChild,
    ) : StatelessFormula<Unit, Int>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
            val firstOutput = context.child(first)
            val second = context.child(second, firstOutput.onIncrement)
            return Evaluation(firstOutput.value)
        }
    }
}