package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.test.test
import org.junit.Test

class FormulaRuntimeTest {

    @Test
    fun `transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        TransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test()
            .renderModel { sideEffectTransition() }
            .renderModel { sideEffectTransition() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        ChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test(defaultToRealFormula = true)
            .renderModel { child.sideEffectTransition() }
            .renderModel { child.sideEffectTransition() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {
        val sideEffectService = SideEffectService()
        NestedChildTransitionAfterNoEvaluationPass
            .formula(sideEffectService)
            .test(defaultToRealFormula = true)
            .renderModel { child.child.sideEffectTransition() }
            .renderModel { child.child.sideEffectTransition() }
            .assertRenderModelCount(1)
            .apply {
                assertThat(sideEffectService.invoked).isEqualTo(2)
            }
    }
}
