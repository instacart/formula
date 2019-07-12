package com.instacart.formula

import com.instacart.formula.test.test
import org.junit.Test

class IntegrationTest {

    @Test
    fun `transition after no re-evaluation pass`() {
        TransitionAfterNoEvaluationPass()
            .test()
            .renderModel { transition() }
            .renderModel { transition() }
            .assertRenderModelCount(1)
    }

    @Test
    fun `child transition after no re-evaluation pass`() {
        ChildTransitionAfterNoEvaluationPass()
            .test(defaultToRealFormula = true)
            .renderModel { child.transition() }
            .renderModel { child.transition() }
            .assertRenderModelCount(1)
    }
}
