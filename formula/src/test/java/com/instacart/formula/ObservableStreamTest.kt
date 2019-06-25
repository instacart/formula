package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test
import java.lang.IllegalStateException

class ObservableStreamTest {

    @Test fun `pass observable directly`() {
        test { state ->
            events("increment", Observable.just(1)) {
                Transition(state + 1)
            }
        }.assertNoErrors().assertValues(1)
    }

    @Test fun `same key crashes`() {
        test { state ->
            events("same", Observable.just(1)) {
                Transition(state)
            }

            events("same", Observable.just(1)) {
                Transition(state)
            }
        }.assertError {
            it is IllegalStateException
        }
    }

    private fun test(
        builder: FormulaContext.StreamBuilder<Int, Unit>.(state: Int) -> Unit
    ) = formula(builder).state(Unit).test()

    private fun formula(
        builder: FormulaContext.StreamBuilder<Int, Unit>.(state: Int) -> Unit
    ): ProcessorFormula<Unit, Int, Unit, Int> {
        return object : ProcessorFormula<Unit, Int, Unit, Int> {
            override fun initialState(input: Unit): Int = 0

            override fun process(input: Unit, state: Int, context: FormulaContext<Int, Unit>): ProcessResult<Int> {

                return ProcessResult(
                    renderModel = state,
                    streams = context.streams {
                        builder(state)
                    }
                )
            }
        }
    }
}
