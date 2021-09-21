package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.MultipleEffectTest.TestFormula2.Event
import com.instacart.formula.rxjava3.RxStream
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.test
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class MultipleEffectTest {

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
                    start = context.onEvent<Int> {
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
}
