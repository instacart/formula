package com.instacart.formula

import com.instacart.formula.NestedTerminationWithInputChanged.RenderModel

/**
 * In a single formula pass:
 * 1. Parent formula provides a new state by `onInputChanged()`
 * 2. Then parent declares a callback.
 * 3. A child formula fires a termination event.
 * 4. Because Frame object wasn't updated to have the new state, a new evaluation is triggered
 * 5. Parent tries to declare a callback in a bad state.e
 */
class NestedTerminationWithInputChanged: Formula<Boolean, Boolean, RenderModel> {
    object RenderModel

    val terminateFormula = TerminateFormula()
    private val passThroughFormula = object : StatelessFormula<Boolean, Unit>() {
        override fun evaluate(input: Boolean, context: FormulaContext<Unit>): Evaluation<Unit> {
            if (input) {
                context.child(terminateFormula)
            }
            return Evaluation(
                renderModel = Unit
            )
        }
    }

    override fun initialState(input: Boolean): Boolean = input

    override fun onInputChanged(oldInput: Boolean, input: Boolean, state: Boolean): Boolean {
        return input
    }

    override fun evaluate(
        input: Boolean,
        state: Boolean,
        context: FormulaContext<Boolean>
    ): Evaluation<RenderModel> {
        // We use a callback to check if formula runtime is in the right state.
        context.callback { none() }
        context.child(passThroughFormula, state)

        return Evaluation(
            renderModel = RenderModel
        )
    }
}
