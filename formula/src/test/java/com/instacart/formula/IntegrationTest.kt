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
}
