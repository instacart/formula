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
        val implementation = original.implementation() as Formula<Unit, Any, Unit>
        return object : IFormula<Unit, Unit> {
            override fun type(): KClass<*> {
                return original.type()
            }

            override fun implementation(): Formula<Unit, *, Unit> {
                return object : Formula<Unit, Any, Unit> by implementation {
                    override fun key(input: Unit): Any? = key
                }
            }
        }
    }
}
