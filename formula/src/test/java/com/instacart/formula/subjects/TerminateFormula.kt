package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import kotlin.reflect.KClass

class TerminateFormula : StatelessFormula<Unit, Unit>() {
    var timesTerminateCalled = 0

    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            actions = context.actions {
                Action.onTerminate().onEvent {
                    transition { timesTerminateCalled += 1 }
                }
            }
        )
    }

    // TODO: maybe at some point I will make this a generic function.
    fun id(key: Any): IFormula<Unit, Unit> {
        val original = this
        return WithId(implementation, original.type(), key)
    }

    internal class WithId<Input, State, Output>(
        override val implementation: Formula<Input, State, Output>,
        val type: Class<*>,
        val key: Any
    ) : IFormula<Input, Output> {
        override fun type(): Class<*> = type

        override fun key(input: Input): Any = key
    }
}
