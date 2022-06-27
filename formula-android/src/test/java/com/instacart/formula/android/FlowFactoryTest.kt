package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.NoOpFeatureFactory
import com.instacart.formula.android.fakes.TestAccountFragmentKey
import com.instacart.formula.android.fakes.TestLoginFragmentKey
import com.instacart.formula.android.fakes.TestSignUpFragmentKey
import com.instacart.formula.android.internal.Binding
import org.junit.Test

class FlowFactoryTest {

    class AuthFlowFactory : FlowFactory<Unit, Unit> {
        override fun createComponent(dependencies: Unit): DisposableScope<Unit> {
            return DisposableScope(Unit) {}
        }

        override fun createFlow(): Flow<Unit> {
            return Flow.build {
                bind(NoOpFeatureFactory<TestLoginFragmentKey>())
                bind(NoOpFeatureFactory<TestSignUpFragmentKey>())
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
        Truth.assertThat(binding.binds(TestLoginFragmentKey())).isTrue()
        Truth.assertThat(binding.binds(TestSignUpFragmentKey())).isTrue()

        Truth.assertThat(binding.binds(TestAccountFragmentKey())).isFalse()
    }

    @Test
    fun `bind flow factory with Any dependency type`() {
        val store = FragmentFlowStore.init("Component") {
            // If it compiles, it's a success
            bind(EmptyFlowFactory<Any>())
        }
    }

    @Test
    fun `bind flow factory with to dependencies defined`() {
        val flowFactoryWithStringDependencies = EmptyFlowFactory<String>()
        val store = FragmentFlowStore.init(100) {
            // If it compiles, it's a success
            bind(flowFactoryWithStringDependencies) { component ->
                component.toString()
            }
        }
    }
}