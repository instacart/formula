package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.messages.TestEventCallback
import com.instacart.formula.test.test
import io.reactivex.Observable
import org.junit.Test

class MultipleEffectTest {

    @Test fun `multiple message order should be maintained`() {
        val triggerEventHandler = TestEventCallback<Int>()
        TestFormula()
            .test(TestFormula.Input(trigger = triggerEventHandler))
            .apply {
                assertThat(triggerEventHandler.values()).containsExactly(0, 1, 2, 3)
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
                renderModel = Unit,
                updates = context.updates {
                    events(Observable.range(0, 4)) {
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
