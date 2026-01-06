package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.test.test
import kotlinx.coroutines.CoroutineScope

object StreamInitMessageDeliveredOnce {
    fun test(scope: CoroutineScope) = TestFormula().test(scope).input(Unit)

    class TestFormula : StatelessFormula<Unit, Unit>() {
        var timesInitializedCalled = 0

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {

            return Evaluation(
                output = Unit,
                actions = context.actions {
                    Action.onInit().onEvent {
                        transition { timesInitializedCalled += 1 }
                    }
                }
            )
        }
    }
}
