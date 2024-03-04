package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

object ExtremelyNestedFormula {
    class TestFormula(private val childFormula: Formula<Unit, *, Int>?) : Formula<Unit, Int, Int>() {
        override fun initialState(input: Unit): Int = 0

        override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {
            val childValue = if (childFormula != null) {
                context.child(childFormula)
            } else {
                0
            }

            return Evaluation(
                output = state + childValue,
                actions = context.actions {
                    Action.onInit().onEvent {
                        transition(state + 1)
                    }
                }
            )
        }
    }

    fun nested(levels: Int): TestFormula {
        return (1 until levels).fold(TestFormula(null)) { child, _ ->
            TestFormula(child)
        }
    }
}
