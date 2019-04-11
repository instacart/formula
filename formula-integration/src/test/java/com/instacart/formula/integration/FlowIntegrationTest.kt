package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FlowIntegrationTest {
    class Integration : FlowIntegration<TestAuthFlowDeclaration.Input> {
        override val flowDeclaration = TestAuthFlowDeclaration()

        override fun input(): TestAuthFlowDeclaration.Input {
            return TestAuthFlowDeclaration.Input(onAuthCompleted = {
                // do
            })
        }
    }

    @Test fun binds() {
        val binding = Integration().binding()
        assertThat(binding.binds(TestLoginFragmentContract())).isTrue()
        assertThat(binding.binds(TestSignUpFragmentContract())).isTrue()
        assertThat(binding.binds(TestAccountFragmentContract())).isFalse()
    }
}
