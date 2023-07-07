package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.Relay
import com.instacart.formula.test.TestListener
import com.instacart.formula.test.TestableRuntime

class ExecuteEffectAfterChildTerminatesRobot(runtime: TestableRuntime) {
    // Relay to connect first and second child formulas
    private val relay = runtime.newRelay()
    // First child notifies the relay which notifies the second child listener
    private val first = FirstChild(relay::triggerEvent)

    private val effectListener = TestListener<Unit>()
    private val second = SecondChild(relay, onEvent = {
        effectListener.invoke(Unit)
    })
    private val observer = runtime.test(Parent(first, second))

    fun start(input: Unit) {
        observer.input(input)
    }

    fun callActionToTerminate() = apply {
        observer.output { this.first.onAction() }
    }

    fun assertEffectRelayCalled(times: Int) = apply {
        effectListener.assertTimesCalled(times)
    }

    // We have to trigger a listener to
    class FirstChild(val actionEffect: () -> Unit) : Formula<Unit, FirstChild.State, FirstChild.Output>() {
        data class State(
            val showSecondChild: Boolean = true,
        )

        data class Output(
            val showSecondChild: Boolean,
            val onAction: () -> Unit,
        )

        override fun initialState(input: Unit): State = State()

        override fun Snapshot<Unit, State>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    showSecondChild = state.showSecondChild,
                    onAction = context.callback {
                        val newState = state.copy(showSecondChild = false)
                        transition(newState) {
                            actionEffect()
                        }
                    }
                )
            )
        }
    }

    class SecondChild(val events: Relay, val onEvent: () -> Unit): Formula<Unit, Int, Int>() {
        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    events.action().onEvent {
                        transition(state + 1) {
                            onEvent()
                        }
                    }
                }
            )
        }
    }

    class Parent(val first: FirstChild, val second: SecondChild): StatelessFormula<Unit, Parent.Output>() {
        data class Output(
            val first: FirstChild.Output,
            val second: Int?,
        )

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Output> {
            val firstOutput = context.child(first)
            val secondOutput = if (firstOutput.showSecondChild) {
                context.child(second)
            } else {
                null
            }
            return Evaluation(
                output = Output(
                    first = firstOutput,
                    second = secondOutput,
                )
            )
        }
    }
}