package com.instacart.formula

import com.instacart.formula.utils.TestUtils

object RemovingTerminateStreamSendsNoMessagesFormula {

    data class Input(
        val onTerminate: (() -> Unit)? = null
    )

    fun create() = TestUtils.stateless { input: Input, context ->
        Evaluation(
            renderModel = Unit,
            updates = context.updates {
                if (input.onTerminate != null) {
                    events(Stream.onTerminate()) {
                        message(input.onTerminate)
                    }
                }
            }
        )
    }
}
