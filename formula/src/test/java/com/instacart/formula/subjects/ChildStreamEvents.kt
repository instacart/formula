package com.instacart.formula.subjects

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.invoke
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope

class ChildStreamEvents(scope: CoroutineScope) {

    private val child = StartStopFormula()
    private val subject = HasChildFormula(child).test(scope).input(Unit)

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
