package com.instacart.formula.subjects

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.invoke
import com.instacart.formula.test.test

class RootFormulaKeyTestSubject() {

    private var input: Int = 0
    private val subject = MyFormula.test().input(input)

    fun increment() = apply {
        subject.output { this.increment() }
    }

    fun assertValue(value: Int) = apply {
        subject.output {
            assertThat(this.value).isEqualTo(value)
        }
    }

    fun resetKey() = apply {
        input += 1
        subject.input(input)
    }


    data class Output(
        val value: Int,
        val increment: Listener<Unit>,
    )

    object MyFormula : Formula<Int, Int, Output>() {
        override fun initialState(input: Int): Int = 0

        // We reset formula whenever key changes.
        override fun key(input: Int): Any? = input

        override fun Snapshot<Int, Int>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    value = state,
                    increment = context.onEvent {
                        transition(state + 1)
                    }
                )
            )
        }
    }
}