package com.instacart.formula

import com.instacart.formula.test.test
import org.junit.Test

class IntegrationTest {

    @Test
    fun `transition after no re-evaluation pass`() {
        TransitionAfterNoEvaluationPass
            .formula()
            .test()
            .renderModel { transition() }
            .renderModel { transition() }
            .assertRenderModelCount(1)
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        ChildTransitionAfterNoEvaluationPass
            .formula()
            .test(defaultToRealFormula = true)
            .renderModel { child.transition() }
            .renderModel { child.transition() }
            .assertRenderModelCount(1)
    }

    @Test
    fun `nested child transition after no re-evaluation pass`() {
        NestedChildTransitionAfterNoEvaluationPass
            .formula()
            .test(defaultToRealFormula = true)
            .renderModel { child.child.transition() }
            .renderModel { child.child.transition() }
            .assertRenderModelCount(1)
    }
}
