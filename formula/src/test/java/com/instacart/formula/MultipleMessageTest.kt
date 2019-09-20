package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.messages.TestEventCallback
import com.instacart.formula.test.test
import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class MultipleMessageTest {

    @Test fun `multiple message order should be maintained`() {
        val triggerEventHandler = TestEventCallback<Int>()
        create()
            .test(Input(trigger = triggerEventHandler))
            .apply {
                assertThat(triggerEventHandler.values()).containsExactly(0, 1, 2, 3)
            }
    }

    data class Input(
        val trigger: (Int) -> Unit
    )

    fun create() = TestUtils.create(0) { input: Input, state, context ->
        Evaluation(
            renderModel = Unit,
            updates = context.updates {
                events(Observable.range(0, 4)) {
                    val updated = state + 1
                    updated.withMessages {
                        message(input.trigger, state)
                    }
                }
            }
        )
    }
}
