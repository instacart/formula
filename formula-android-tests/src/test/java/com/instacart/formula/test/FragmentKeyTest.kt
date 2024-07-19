package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class FragmentKeyTest {

    @Test
    fun `default tag is the class name`() {
        assertThat(FragmentKeyWithDefaultTag.tag).isEqualTo("FragmentKeyWithDefaultTag")
    }
}