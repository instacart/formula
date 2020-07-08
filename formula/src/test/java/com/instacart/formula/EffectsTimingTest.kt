package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class EffectsTimingTest {

    @Test fun `side effect triggers a state change`() {
        val relay = PublishRelay.create<Unit>()
        val input = Input(
            external = relay,
            trigger = {
                relay.accept(Unit)
            }
        )

        TestFormula()
            .test(input)
            .output { trigger() }
            .output {
                assertThat(events).containsExactly(State.INTERNAL, State.EXTERNAL).inOrder()
            }
    }

    class Input(
        val external: Observable<Unit>,
        val trigger: () -> Unit
    )

    enum class State {
        INTERNAL,
        EXTERNAL
    }

    data class Output(val events: List<State>, val trigger: () -> Unit)

    class TestFormula : Formula<Input, List<State>, Output> {

        override fun initialState(input: Input): List<State> = emptyList()

        override fun evaluate(
            input: Input,
            state: List<State>,
            context: FormulaContext<List<State>>
        ): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    events = state,
                    trigger = context.callback {
                        transition(state.plus(State.INTERNAL)) {
                            input.trigger()
                        }
                    }
                ),
                updates = context.updates {
                    events(input.external) {
                        transition(state.plus(State.EXTERNAL))
                    }
                }
            )
        }
    }
}
