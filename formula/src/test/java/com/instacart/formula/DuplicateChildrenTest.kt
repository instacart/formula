package com.instacart.formula

import com.instacart.formula.utils.TestUtils
import org.junit.Test
import java.lang.IllegalStateException

class DuplicateChildrenTest {

    @Test fun `adding duplicate child throws an exception`() {
        parent().start(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    fun parent() = TestUtils.stateless { context ->
        Evaluation(
            renderModel = listOf(1, 2, 3).map {
                context.child(child()).input(Unit)
            }
        )
    }

    fun child() = TestUtils.stateless { context ->
        Evaluation(renderModel = Unit)
    }
}
