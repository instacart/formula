package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.test.TestAccountFragmentContract
import com.instacart.formula.integration.test.TestLoginFragmentContract
import com.instacart.formula.integration.test.TestSignUpFragmentContract
import io.reactivex.Flowable
import org.junit.Test
import kotlin.reflect.KClass

class FlowIntegrationTest {
    class MyFlowDeclaration(
        val types: List<KClass<out FragmentContract<*>>>
    ) : FlowDeclaration<Unit>() {
        override fun createFlow(): Flow<Unit> {
            return build {
                types.forEach {
                    bind(it) { component, key ->
                        Flowable.empty()
                    }
                }
            }
        }
    }

    class AuthFlowIntegration : FlowIntegration<Unit, Unit>() {
        override val flowDeclaration = MyFlowDeclaration(
            types = listOf(
                TestLoginFragmentContract::class, TestSignUpFragmentContract::class
            )
        )

        override fun createComponent(parentComponent: Unit): DisposableScope<Unit> {
            return DisposableScope(Unit) {}
        }
    }

    @Test fun `binds only declared contracts`() {

        val binding = AuthFlowIntegration().binding()
        assertThat(binding.binds(TestLoginFragmentContract())).isTrue()
        assertThat(binding.binds(TestSignUpFragmentContract())).isTrue()

        assertThat(binding.binds(TestAccountFragmentContract())).isFalse()
    }
}
