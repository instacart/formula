package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test

class ChildRemovedOnMessage {
    private val subject = OptionalChildFormula(
        child = MessageFormula(),
        childInput = { state ->
            MessageFormula.Input(
                messageHandler = onEvent<Int> {
                    transition(state.copy(showChild = false))
                }
            )
        })
        .test(Unit)

    fun assertChildIsVisible(visible: Boolean) = apply {
        subject.output {
            if (visible) {
                Truth.assertThat(child).isNotNull()
            } else {
                Truth.assertThat(child).isNull()
            }
        }
    }

    fun closeByChildMessage() = apply {
        subject.output { child!!.incrementAndMessage() }
    }
}
