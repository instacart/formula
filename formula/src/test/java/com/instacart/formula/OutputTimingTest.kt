package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class OutputTimingTest {

    @Test fun `output state change should use latest state`() {
        val relay = PublishRelay.create<Unit>()
        TestFormula()
            .state(Input(relay)) {
                relay.accept(Unit)
            }
            .test()
            .apply {
                values().last().trigger()
            }
            .apply {
                assertThat(values().map { it.value }).containsExactly("internal", "external")
            }
    }

    class Trigger
    class Input(val external: Observable<Unit>)
    class RenderModel(val value: String, val trigger: () -> Unit)

    class TestFormula : Formula<Input, String, Trigger, RenderModel> {
        companion object {
            const val LAST_STATE_INTERNAL = "internal"
            const val LAST_STATE_EXTERNAL = "external"
        }

        override fun initialState(input: Input): String = LAST_STATE_INTERNAL

        override fun evaluate(
            input: Input,
            state: String,
            context: FormulaContext<String, Trigger>
        ): Evaluation<RenderModel> {
            return Evaluation(
                renderModel = RenderModel(
                    value = state,
                    trigger = context.callback("trigger") {
                        transition(LAST_STATE_INTERNAL, Trigger())
                    }
                ),
                updates = context.updates {
                    events("changes", input.external) {
                        transition(LAST_STATE_EXTERNAL)
                    }
                }
            )
        }
    }
}
