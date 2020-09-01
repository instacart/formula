package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import com.jakewharton.rxrelay3.BehaviorRelay

class RootFormulaKeyTestSubject {

    private val inputRelay = BehaviorRelay.createDefault(0)
    private val subject = MyFormula.test(inputRelay)

    fun increment() = apply {
        subject.output { increment() }
    }

    fun assertValue(value: Int) = apply {
        subject.output {
            assertThat(this.value).isEqualTo(value)
        }
    }

    fun resetKey() = apply {
        inputRelay.accept(inputRelay.value + 1)
    }


    data class Output(
        val value: Int,
        val increment: () -> Unit
    )

    object MyFormula : Formula<Int, Int, Output> {
        override fun initialState(input: Int): Int = 0

        // We reset formula whenever key changes.
        override fun key(input: Int): Any? = input

        override fun evaluate(
            input: Int,
            state: Int,
            context: FormulaContext<Int>
        ): Evaluation<Output> {
            return Evaluation(
                output = Output(
                    value = state,
                    increment = context.callback {
                        transition(state + 1)
                    }
                )
            )
        }
    }
}