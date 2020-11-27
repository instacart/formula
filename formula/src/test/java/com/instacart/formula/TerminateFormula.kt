package com.instacart.formula

import kotlin.reflect.KClass

class TerminateFormula : StatelessFormula<Unit, Unit>() {
    var timesTerminateCalled = 0

    override fun evaluate(input: Unit, context: FormulaContext<Unit>): Evaluation<Unit> {
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

    internal class WithId<Input, State : Any, Output>(
        val implementation: Formula<Input, State, Output>,
        val type: KClass<*>,
        val key: Any
    ) : IFormula<Input, Output> {
        override fun type(): KClass<*> = type

        override fun implementation(): Formula<Input, *, Output> = implementation

        override fun key(input: Input): Any = key
    }
}
