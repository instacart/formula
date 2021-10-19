package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.IFormula
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.Stream
import kotlin.reflect.KClass

class TerminateFormula : StatelessFormula<Unit, Unit>() {
    var timesTerminateCalled = 0

    override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Unit> {
        return Evaluation(
            output = Unit,
            updates = context.updates {
                Stream.onTerminate().onEvent {
                    transition { timesTerminateCalled += 1 }
                }
            }
        )
    }

    // TODO: maybe at some point I will make this a generic function.
    fun id(key: Any): IFormula<Unit, Unit> {
        val original = this
        return WithId(implementation(), original.type(), key)
    }

    internal class WithId<Input, State, Output>(
        val implementation: Formula<Input, State, Output>,
        val type: KClass<*>,
        val key: Any
    ) : IFormula<Input, Output> {
        override fun type(): KClass<*> = type

        override fun implementation(): Formula<Input, *, Output> = implementation

        override fun key(input: Input): Any = key
    }
}
