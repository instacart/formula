package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test
import java.lang.IllegalStateException

class ObservableStreamTest {

    @Test fun `pass observable directly`() {
        test { state ->
            events("increment", Observable.just(1)) {
                (state + 1).noMessages()
            }
        }.assertNoErrors().assertValues(1)
    }

    @Test fun `same key crashes`() {
        test { state ->
            events("same", Observable.just(1)) {
                none()
            }

            events("same", Observable.just(1)) {
                none()
            }
        }.assertError {
            it is IllegalStateException
        }
    }

    private fun test(
        builder: FormulaContext.UpdateBuilder<Int>.(state: Int) -> Unit
    ) = formula(builder).state(Unit).test()

    private fun formula(
        builder: FormulaContext.UpdateBuilder<Int>.(state: Int) -> Unit
    ): Formula<Unit, Int, Int> {
        return object : Formula<Unit, Int, Int> {

            override fun initialState(input: Unit): Int = 0

            override fun evaluate(input: Unit, state: Int, context: FormulaContext<Int>): Evaluation<Int> {

                return Evaluation(
                    renderModel = state,
                    updates = context.updates {
                        builder(state)
                    }
                )
            }
        }
    }
}
