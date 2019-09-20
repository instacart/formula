package com.instacart.formula

import com.instacart.formula.test.messages.TestCallback
import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class SimpleSideEffectTest {

    @Test
    fun `side effect test`() {
        val intRange = 1..100
        val gameOverCallback = TestCallback()
        create(
            increment = Observable.fromIterable(intRange.map { Unit }),
            onGameOver = gameOverCallback
        ).test()

        gameOverCallback.assertTimesCalled(1)
    }

    data class State(val count: Int)

    fun create(
        increment: Observable<Unit>,
        onGameOver: () -> Unit
    ) = TestUtils.create(State(0)) { state, context ->
        Evaluation(
            renderModel = state.count,
            updates = context.updates {
                events(increment) {
                    val updated = state.copy(count = state.count + 1)

                    if (updated.count == 5) {
                        updated.withMessage(onGameOver)
                    } else {
                        updated.noMessages()
                    }
                }
            }
        )
    }
}
