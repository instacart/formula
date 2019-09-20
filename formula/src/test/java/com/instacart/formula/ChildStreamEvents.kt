package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test

class ChildStreamEvents {

    private val incrementRelay = IncrementRelay()
    private val child = StreamFormula(incrementRelay)
    private val subject = HasChildFormula.create(child).test()

    fun startListening() = apply {
        subject.renderModel { child.startListening() }
    }

    fun stopListening() = apply {
        subject.renderModel { child.stopListening() }
    }

    fun incrementBy(step: Int) = apply {
        val range = 1..step
        range.forEach {
            incrementRelay.triggerIncrement()
        }
    }

    fun assertCurrentValue(value: Int) = apply {
        subject.renderModel { assertThat(child.state).isEqualTo(value) }
    }
}
