package com.instacart.formula.counter

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.ProcessorFormula
import io.reactivex.Observable
import org.junit.Test

class CounterProcessorFormulaTest {

    @Test fun `increment 5 times`() {

        CounterProcessorFormula()
            .test(Unit)
            .call { onIncrement() }
            .call { onIncrement() }
            .call { onIncrement() }
            .call { onIncrement() }
            .call { onIncrement() }
            .check {
               assertThat(this.count).isEqualTo("Count: 5")
            }
    }

    fun <Input, RenderModel> ProcessorFormula<Input, *, *, RenderModel>.test(
        input: Input
    ): ProcessorFormulaTestSubject<RenderModel> {
        return ProcessorFormulaTestSubject(state(input))
    }


    class ProcessorFormulaTestSubject<RenderModel>(private val stream: Observable<RenderModel>) {
        @PublishedApi internal val subject = stream.test()

        inline fun call(func: RenderModel.() -> Unit) = apply {
            subject.values().last().func()
        }


        inline fun check(func: RenderModel.() -> Unit) = apply {
            subject.values().last().func()
        }
    }
}
