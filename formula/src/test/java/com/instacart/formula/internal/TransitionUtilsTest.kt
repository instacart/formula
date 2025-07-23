package com.instacart.formula.internal

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Effect
import com.instacart.formula.Transition
import com.instacart.formula.plugin.FormulaError
import org.junit.Test

class TransitionUtilsTest {

    @Test fun `none transition is empty`() {
        val none = Transition.Result.None
        assertThat(TransitionUtils.isEmpty(none)).isTrue()
    }

    @Test fun `state change is not empty`() {
        val transition = Transition.Result.Stateful(state = "")
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }

    @Test fun `transition with messages is not empty`() {
        val effect = Effect(FakeEffectDelegate(), Effect.Main) {}
        val transition = Transition.Result.OnlyEffects(effects = listOf(effect))
        assertThat(TransitionUtils.isEmpty(transition)).isFalse()
    }
}
