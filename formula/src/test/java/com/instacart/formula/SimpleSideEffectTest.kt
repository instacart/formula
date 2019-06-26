package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import org.junit.Test

class SimpleSideEffectTest {

    @Test fun `side effect test`() {
        val intRange = 1..100
        var gameOverCalls = 0
        TestFormula(
            increment = Observable.fromIterable(intRange.map { Unit }),
            onGameOver = {
                gameOverCalls += 1
            }
        )
            .state(Unit)
            .test()
            .assertNoErrors()

        assertThat(gameOverCalls).isEqualTo(1)
    }

    class TestFormula(
        private val increment: Observable<Unit>,
        private val onGameOver: () -> Unit
    ) : ProcessorFormula<Unit, TestFormula.State, Unit, Int> {
        data class State(val count: Int)

        override fun initialState(input: Unit): State = State(count = 0)

        override fun process(input: Unit, state: State, context: FormulaContext<State, Unit>): ProcessResult<Int> {
            return ProcessResult(
                renderModel = state.count,
                updates = context.updates {
                    events("increment", increment) {
                        Transition(state.copy(count = state.count + 1))
                    }

                    if (state.count == 5) {
                        effect("result", onGameOver)
                    }
                }
            )
        }
    }
}
