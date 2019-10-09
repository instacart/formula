package com.instacart.formula

import com.instacart.formula.test.messages.TestCallback
import com.instacart.formula.test.test
import io.reactivex.Observable
import org.junit.Test

class SimpleSideEffectTest {

    @Test fun `side effect test`() {
        val intRange = 1..100
        val gameOverCallback = TestCallback()
        TestFormula(
            increment = Observable.fromIterable(intRange.map { Unit }),
            onGameOver = gameOverCallback
        ).test()

        gameOverCallback.assertTimesCalled(1)
    }

    class TestFormula(
        private val increment: Observable<Unit>,
        private val onGameOver: () -> Unit
    ) : Formula<Unit, TestFormula.State, Int> {
        data class State(val count: Int)

        override fun initialState(input: Unit): State = State(count = 0)

        override fun evaluate(input: Unit, state: State, context: FormulaContext<State>): Evaluation<Int> {
            return Evaluation(
                renderModel = state.count,
                updates = context.updates {
                    events(increment) {
                        val updated = state.copy(count = state.count + 1)

                        transition(updated) {
                            if (updated.count == 5) {
                                onGameOver()
                            }
                        }
                    }
                }
            )
        }
    }
}
