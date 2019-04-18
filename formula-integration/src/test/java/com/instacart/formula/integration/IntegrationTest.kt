package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
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

    class MyIntegration : UnscopedFormulaIntegration<String, MyFormula.Input, String>() {
        override fun createFormula(key: String): Formula<MyFormula.Input, String> {
            return MyFormula()
        }

        override fun input(key: String): MyFormula.Input {
            return MyFormula.Input(prefix = key)
        }
    }

    @Test fun `integration invoke method`() {
        val state = MyIntegration().create(Unit, "aha")

        state.test().assertValues("aha - 0", "aha - 1", "aha - 2")
    }

    @Test fun `bind integration`() {
        val backStack = BackStack.empty<String>().add("key")
        val store = FlowStore.init(state = Flowable.just(backStack)) {
            bind(MyIntegration())
        }

        val models = store.state().test().values().mapNotNull {
            it.lastEntry()?.renderModel
        }
        assertThat(models).containsExactly("key - 0", "key - 1", "key - 2")
    }
}
