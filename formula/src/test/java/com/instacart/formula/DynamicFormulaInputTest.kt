package com.instacart.formula

import com.instacart.formula.utils.TestUtils
import io.reactivex.Observable
import org.junit.Test

class DynamicFormulaInputTest {

    @Test fun `using dynamic input`() {
        formula()
            .start(input = Observable.just(1, 2, 3))
            .test()
            .assertValues(1, 2, 3)
    }

    fun formula() = TestUtils.stateless { input: Int, context ->
        Evaluation(renderModel = input)
    }
}
