package com.instacart.formula

import org.junit.Test
import java.lang.IllegalStateException

class DuplicateChildrenTest {

    @Test fun `adding duplicate child throws an exception`() {
        ParentFormula().state(Unit).test().assertError {
            it is IllegalStateException
        }
    }

    class ParentFormula : ProcessorFormula<Unit, Unit, Unit, List<Unit>> {
        override fun initialState(input: Unit) = Unit

        override fun process(input: Unit, state: Unit, context: FormulaContext<Unit, Unit>): ProcessResult<List<Unit>> {
            return ProcessResult(
                renderModel = listOf(1, 2, 3).map {
                    context.child(ChildFormula(), Unit) {
                        Transition(state)
                    }
                }
            )
        }
    }

    class ChildFormula: ProcessorFormula<Unit, Unit, Unit, Unit> {
        override fun initialState(input: Unit) = Unit

        override fun process(input: Unit, state: Unit, context: FormulaContext<Unit, Unit>): ProcessResult<Unit> {
            return ProcessResult(
                renderModel = Unit
            )
        }
    }
}
