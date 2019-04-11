package com.instacart.formula.integration

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.integration.test.TestAccountFragmentContract
import com.instacart.formula.integration.test.TestAuthFlowIntegration
import com.instacart.formula.integration.test.auth.TestLoginFragmentContract
import com.instacart.formula.integration.test.auth.TestSignUpFragmentContract
import org.junit.Test

class FlowIntegrationTest {
    @Test fun binds() {
        val binding = TestAuthFlowIntegration().binding()
        assertThat(binding.binds(TestLoginFragmentContract())).isTrue()
        assertThat(binding.binds(TestSignUpFragmentContract())).isTrue()

        assertThat(binding.binds(TestAccountFragmentContract())).isFalse()
    }
}
