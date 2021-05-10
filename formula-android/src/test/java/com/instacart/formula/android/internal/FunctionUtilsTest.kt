package com.instacart.formula.android.internal

import com.google.common.truth.Truth
import org.junit.Test

class FunctionUtilsTest {

    @Test
    fun `identity is optimized to return the same value`() {
        val stringIdentity = FunctionUtils.identity<String>()
        val intIdentity = FunctionUtils.identity<Int>()
        Truth.assertThat(stringIdentity).isEqualTo(intIdentity)
    }
}