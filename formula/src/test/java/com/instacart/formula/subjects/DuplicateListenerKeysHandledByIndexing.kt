package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope

object DuplicateListenerKeysHandledByIndexing {

    fun test(scope: CoroutineScope) = TestFormula().test(scope).input(Unit)

    data class TestOutput(
        val listeners: List<Listener<Unit>>,
    )

    class TestFormula : StatelessFormula<Unit, TestOutput>() {
        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<TestOutput> {
            val keys = listOf(1, 2, 1, 1, 1)
            return Evaluation(
                output = TestOutput(
                    listeners = keys.map { key ->
                        context.onEvent(key) { none() }
                    }
                )
            )
        }
    }
}
