package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.rxjava3.RxAction
import com.instacart.formula.test.FlowRelay
import com.instacart.formula.test.TestableRuntime
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.flowOf

object SubscribesToAllUpdatesBeforeDeliveringMessages {

    fun test(runtime: TestableRuntime) = runtime.test(TestFormula(), Unit)

    class TestFormula : Formula<Unit, Int, Int>() {
        private val initial = Action.fromFlow {
            flowOf(Unit, Unit, Unit, Unit)
        }
        
        private val incrementRelay = FlowRelay()

        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    initial.onEvent {
                        transition {
                            incrementRelay.triggerEvent()
                        }
                    }

                    incrementRelay.action().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }
}
