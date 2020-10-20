package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.MultipleEffectTest.TestFormula2.Event
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class MultipleEffectTest {

    @Test fun `multiple effect order should be maintained`() {
        val triggerEventHandler = TestEventCallback<Int>()
        TestFormula()
            .test(TestFormula.Input(trigger = triggerEventHandler))
            .apply {
                val expected = listOf(0, 1, 2, 3)
                assertThat(triggerEventHandler.values()).isEqualTo(expected)
            }
    }

    @Test fun `multiple effect order maintained across multiple event streams`() {
        // A state transition has a side effect and triggers another state transition which triggers a side-effect

        val onEvent = TestEventCallback<Event>()
        TestFormula2()
            .test(TestFormula2.Input(onEvent = onEvent))
            .output { start(1) }

        val expected = listOf(Event.Started(1), Event.Stopped(1))
        assertThat(onEvent.values()).isEqualTo(expected)
    }

    class TestFormula2 : Formula<TestFormula2.Input, TestFormula2.State, TestFormula2.Output> {
        sealed class Event {
            data class Started(val id: Int): Event()
            data class Stopped(val id: Int): Event()
        }

        data class Input(
            val onEvent: (Event) -> Unit
        )


        data class State(
            val started: List<Int> = emptyList()
        )

        data class Output(
            val start: (Int) -> Unit
        )

        override fun initialState(input: Input): State = State()

        override fun evaluate(
            input: Input,
            state: State,
            context: FormulaContext<State>
        ): Evaluation<Output> {
            val started = state.started
            return Evaluation(
                output = Output(
                    start = context.eventCallback {
                        transition(state.copy(started = started.plus(it))) {
                            input.onEvent(Event.Started(it))
                        }
                    }
                ),
                updates = context.updates {
                    for (id in started) {
                        events(Stream.onData(id)) {
                            transition(state.copy(started = started.minus(id))) {
                                input.onEvent(Event.Stopped(id))
                            }
                        }
                    }
                }
            )
        }
    }


    class TestFormula : Formula<TestFormula.Input, Int, Unit> {

        data class Input(
            val trigger: (Int) -> Unit
        )

        override fun initialState(input: Input) = 0

        override fun evaluate(
            input: Input,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Unit> {
            return Evaluation(
                output = Unit,
                updates = context.updates {
                    RxStream.fromObservable { Observable.range(0, 4) }.events {
                        val updated = state + 1
                        transition(updated) {
                            input.trigger(state)
                        }
                    }
                }
            )
        }
    }
}
