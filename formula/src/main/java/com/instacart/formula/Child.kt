package com.instacart.formula

import com.instacart.formula.internal.FormulaContextImpl
import com.instacart.formula.internal.JoinedKey

/**
 * A stateful child [Formula] builder. It is initialized by calling [FormulaContext.child].
 */
class Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> internal constructor(
    @PublishedApi internal val context: FormulaContextImpl<State, Output>
) {
    private val none: Transition.Factory.(ChildOutput) -> Transition<State, Output> = {
        none()
    }

    @PublishedApi internal var key: Any? = null
    private var formula: Formula<ChildInput, *, ChildOutput, ChildRenderModel>? = null
    private var onOutput: Transition.Factory.(ChildOutput) -> Transition<State, Output> = none

    internal fun initialize(key: String, formula: Formula<ChildInput, *, ChildOutput, ChildRenderModel>) {
        if (this.formula != null) {
            throw IllegalStateException("unfinished child definition: ${this.formula}")
        }

        this.key = JoinedKey(key, formula::class)
        this.formula = formula
    }

    internal fun finish() {
        key = null
        formula = null
        onOutput = none
    }

    fun onOutput(
        callback: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): Child<State, Output, ChildInput, ChildOutput, ChildRenderModel> = apply {
        this.onOutput = callback
    }

    inline fun input(crossinline create: () -> ChildInput): ChildRenderModel {
        return context.key(checkNotNull(key)) {
            input(create())
        }
    }

    fun input(input: ChildInput): ChildRenderModel {
        val key = checkNotNull(key)
        val formula = checkNotNull(formula)
        val renderModel = context.child(key, formula, input, onOutput)
        finish()
        return renderModel
    }
}
