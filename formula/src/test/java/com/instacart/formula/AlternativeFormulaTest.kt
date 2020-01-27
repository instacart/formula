package com.instacart.formula

import io.reactivex.Observable
import org.junit.Test

class AlternativeFormulaTest {

    @Test fun `using Stateless formula`() {
        TestStatelessFormula()
            .start(input = Observable.just(1, 2, 3))
            .test()
            .assertValues(1, 2, 3)
    }

    @Test fun `using Inputless formula`() {
        TestInputlessFormula()
            .start()
            .test()
            .assertValues(1)
    }

    @Test fun `using Render formula`() {
        TestRenderFormula()
            .start()
            .test()
            .assertValues(1)
    }

    class TestStatelessFormula:  StatelessFormula<Int, Int>() {
        override fun evaluate(input: Int, context: FormulaContext<Unit>): Evaluation<Int> {
            return Evaluation(renderModel = input)
        }
    }

    class TestInputlessFormula: InputlessFormula<Int, Int>() {

        override fun initialState(input: Unit): Int {
            return 0
        }

        override fun evaluate(state: Int, context: FormulaContext<Int>): Evaluation<Int> {
            return Evaluation(renderModel = 1)
        }
    }

    class TestRenderFormula:  RenderFormula<Int>() {
        override fun evaluate(context: FormulaContext<Unit>): Evaluation<Int> {
            return Evaluation(renderModel = 1)
        }
    }
}
