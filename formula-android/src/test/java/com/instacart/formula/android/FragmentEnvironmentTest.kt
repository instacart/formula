package com.instacart.formula.android

import com.google.common.truth.Truth
import com.instacart.formula.android.fakes.MainKey
import org.junit.Test

class FragmentEnvironmentTest {

    @Test
    fun `default onError throws exception`() {

        val fragmentEnvironment = FragmentEnvironment()
        val mainKey = MainKey(id = 1)
        val exception = RuntimeException("huh")
        try {
            fragmentEnvironment.onScreenError(mainKey, exception)
            error("should not happen")
        } catch (e: Exception) {
            Truth.assertThat(e).isEqualTo(exception)
        }
    }
}