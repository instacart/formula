package com.instacart.formula.subjects

import com.google.common.truth.Truth
import com.instacart.formula.invoke
import com.instacart.formula.test.test

class ChildRemovedOnMessage {
    private val formula = OptionalChildFormula(
        child = MessageFormula(),
        childInput = { state ->
            MessageFormula.Input(
                messageHandler = onEvent<Int> {
                    transition(state.copy(showChild = false))
                }
            )
        })
    private val subject = formula.test().input(Unit)

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
