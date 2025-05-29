package com.instacart.formula.subjects

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.invoke
import com.instacart.formula.test.TestableRuntime

class ChildStreamEvents(runtime: TestableRuntime) {

    private val child = StartStopFormula()
    private val subject = runtime.test(HasChildFormula(child), Unit)

    fun startListening() = apply {
        subject.output { child.startListening() }
    }

    fun stopListening() = apply {
        subject.output { child.stopListening() }
    }

    fun incrementBy(step: Int) = apply {
        val range = 1..step
        range.forEach {
            child.incrementEvents.triggerEvent()
        }
    }

    fun assertCurrentValue(value: Int) = apply {
        subject.output { assertThat(child.state).isEqualTo(value) }
    }
}
