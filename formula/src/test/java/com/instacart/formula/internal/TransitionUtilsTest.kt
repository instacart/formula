package com.instacart.formula.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.SideEffect
import com.instacart.formula.Transition
import org.junit.Test

class TransitionUtilsTest {

    @Test fun `none transition is empty`() {
        val none = Transition.Factory.none<String, String>()
        assertThat(TransitionUtils.isEmpty(none)).isTrue()
    }

    @Test fun `state change is not empty`() {
        val transition = Transition<String, String>(state = "")
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }

    @Test fun `transition with output is not empty`() {
        val transition = Transition<String, String>(output = "")
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }

    @Test fun `transition with side effects is not empty`() {
        val transition = Transition<String, String>(sideEffects = listOf(SideEffect("random", {})))
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }
}