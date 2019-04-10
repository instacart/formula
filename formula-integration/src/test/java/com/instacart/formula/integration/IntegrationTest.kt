package com.instacart.formula.integration

import com.instacart.formula.Formula
import io.reactivex.Flowable
import org.junit.Test

class IntegrationTest {

    class MyFormula : Formula<MyFormula.Input, String> {
        class Input(
            val prefix: String
        )

        override fun state(input: Input): Flowable<String> {
            return Flowable.just(0, 1, 2).map {
                "${input.prefix} - $it"
            }
        }
    }

    class MyIntegration : Integration<String, MyFormula.Input, String> {
        override fun createFormula(key: String): Formula<MyFormula.Input, String> {
            return MyFormula()
        }

        override fun input(key: String): MyFormula.Input {
            return MyFormula.Input(prefix = key)
        }
    }


    @Test fun `integration invoke method`() {
        val state = MyIntegration().invoke("aha")

        state.test().assertValues("aha - 0", "aha - 1", "aha - 2")
    }
}
