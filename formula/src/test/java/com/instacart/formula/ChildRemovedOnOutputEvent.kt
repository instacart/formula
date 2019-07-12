package com.instacart.formula

import com.google.common.truth.Truth
import com.instacart.formula.test.test

class ChildRemovedOnOutputEvent {
    private val subject = OptionalChildFormula(OutputFormula(), onChildOutput = { state, output ->
        val updated = state.copy(showChild = false)
        Transition.Factory.transition(updated, output)
    }).test()

    fun assertChildIsVisible(visible: Boolean) = apply {
        subject.renderModel {
            if (visible) {
                Truth.assertThat(child).isNotNull()
            } else {
                Truth.assertThat(child).isNull()
            }
        }
    }

    fun closeByOutput() = apply {
        subject.renderModel { child!!.incrementAndOutput() }
    }
}
