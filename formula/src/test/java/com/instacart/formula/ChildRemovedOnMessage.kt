package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.TestableRuntime

class ChildRemovedOnMessage(runtime: TestableRuntime) {
    private val formula = OptionalChildFormula(
        child = MessageFormula(),
        childInput = { state ->
            MessageFormula.Input(
                messageHandler = onEvent<Int> {
                    transition(state.copy(showChild = false))
                }
            )
        })
    private val subject = runtime.test(formula, Unit)

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
