package com.instacart.formula.tests

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Stream
import com.instacart.formula.stateless
import com.instacart.formula.test.test
import java.lang.IllegalStateException

object EmitErrorTest {
    fun test() = formula().test()

    private fun formula() = Formula.stateless { context ->
        Evaluation(
            output = Unit,
            updates = context.updates {
                events(Stream.onInit()) {
                    throw IllegalStateException("crashed")
                }
            }
        )
    }
}