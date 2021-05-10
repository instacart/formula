package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.DisposableScope
import com.instacart.formula.android.fakes.TestAccountFragmentContract
import com.instacart.formula.android.fakes.TestLoginFragmentContract
import com.instacart.formula.android.fakes.TestSignUpFragmentContract
import io.reactivex.rxjava3.core.Observable
import org.junit.Test

class FlowFactoryTest {

    class AuthFlowFactory : FlowFactory<Unit, Unit> {
        private val contracts = listOf(
            TestLoginFragmentContract::class,
            TestSignUpFragmentContract::class
        )

        override fun createComponent(dependencies: Unit): DisposableScope<Unit> {
            return DisposableScope(Unit) {}
        }

        override fun createFlow(): Flow<Unit> {
            return Flow.build {
                contracts.forEach {
                    bind(it) { component, key ->
                        Observable.empty()
                    }
                }
            }
        }
    }

    class EmptyFlowFactory<Dependencies> : FlowFactory<Dependencies, Unit> {
        override fun createComponent(dependencies: Dependencies): DisposableScope<Unit> {
            return DisposableScope(Unit, {})
        }

        override fun createFlow(): Flow<Unit> {
            return Flow.build {  }
        }
    }

    @Test
    fun `binds only declared contracts`() {

        val binding = Binding.composite(AuthFlowFactory())
        Truth.assertThat(binding.binds(TestLoginFragmentContract())).isTrue()
        Truth.assertThat(binding.binds(TestSignUpFragmentContract())).isTrue()

        Truth.assertThat(binding.binds(TestAccountFragmentContract())).isFalse()
    }

    @Test
    fun `bind flow factory with Any dependency type`() {
        val store = FragmentFlowStore.init("Component") {
            // If it compiles, it's a success
            bind(EmptyFlowFactory<Any>())
        }
    }
}