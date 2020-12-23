package com.instacart.formula.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Transition
import org.junit.Test

class TransitionUtilsTest {

    @Test fun `none transition is empty`() {
        val none = Transition.Factory.none()
        assertThat(TransitionUtils.isEmpty(none)).isTrue()
    }

    @Test fun `state change is not empty`() {
        val transition = Transition.Stateful(state = "")
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }

    @Test fun `transition with messages is not empty`() {
        val transition = Transition.OnlyEffects(effects = {})
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }

    @Test fun `simple transition create`() {
        val transition = Transition.create {
            transition("new state")
        }

        assertThat(transition.state).isEqualTo("new state")
    }
}
