package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test

class ChildRemovedOnMessage {
    private val subject = OptionalChildFormula(
        child = MessageFormula(),
        childInput = { state ->
            MessageFormula.Input(
                messageHandler = eventCallback {
                    state.copy(showChild = false).noMessages()
                }
            )
        })
        .test()

    fun assertChildIsVisible(visible: Boolean) = apply {
        subject.renderModel {
            if (visible) {
                Truth.assertThat(child).isNotNull()
            } else {
                Truth.assertThat(child).isNull()
            }
        }
    }

    fun closeByChildMessage() = apply {
        subject.renderModel { child!!.incrementAndMessage() }
    }
}
