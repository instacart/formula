package com.instacart.formula.integration

import android.view.View
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Formula
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import org.junit.Test

class FragmentIntegrationTest {

    @Parcelize
    data class TestContract(
        override val tag: String = "test contract",
        override val layoutId: Int = -1
    ) : FragmentContract<String>() {
        override fun createComponent(view: View): FragmentComponent<String> = FragmentComponent.noOp()
    }

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

    class MyIntegration : Integration<TestContract, MyFormula.Input, String>() {
        override fun createFormula(key: TestContract): Formula<MyFormula.Input, String> {
            return MyFormula()
        }

        override fun input(key: TestContract): MyFormula.Input {
            return MyFormula.Input(prefix = key.tag)
        }
    }

    @Test fun `integration invoke method`() {
        val state = MyIntegration().init(TestContract(tag = "aha"))

        state.test().assertValues("aha - 0", "aha - 1", "aha - 2")
    }

    @Test fun `bind integration`() {
        val store = FragmentFlowStore.init {
            bind(MyIntegration())
        }

        store.onLifecycleEffect(LifecycleEvent.Added(TestContract("aha")))

        val models = store.state().test().values().mapNotNull {
            it.lastEntry()?.renderModel
        }
        assertThat(models).containsExactly("aha - 0", "aha - 1", "aha - 2")
    }
}
