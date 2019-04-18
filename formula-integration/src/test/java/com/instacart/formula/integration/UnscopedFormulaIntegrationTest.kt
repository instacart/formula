package com.instacart.formula.integration

import android.view.View
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Formula
import com.instacart.formula.fragment.FragmentComponent
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowStore
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize
import org.junit.Test

class UnscopedFormulaIntegrationTest {

    @Parcelize
    data class TestContract(
        override val tag: String = "test contract",
        override val layoutId: Int = -1
    ) : FragmentContract<String>() {
        override fun createComponent(view: View): FragmentComponent<String> = FragmentComponent.noOp()
    }

    class MyFormula : Formula<String, String> {
        override fun state(input: String): Flowable<String> {
            return Flowable.just(0, 1, 2).map {
                "${input} - $it"
            }
        }
    }

    class MyIntegration : UnscopedFormulaIntegration<TestContract, String, String>() {
        override fun createFormula(key: TestContract): Formula<String, String> {
            return MyFormula()
        }

        override fun input(key: TestContract): String {
            return key.tag
        }
    }

    @Test fun `integration invoke method`() {
        val state = MyIntegration().create(Unit, TestContract(tag = "aha"))

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
