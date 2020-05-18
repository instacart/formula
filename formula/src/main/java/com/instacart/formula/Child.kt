package com.instacart.formula

import com.instacart.formula.internal.FormulaContextImpl
import com.instacart.formula.internal.JoinedKey

/**
 * A stateful child [Formula] builder. It is initialized by calling [FormulaContext.child].
 */
class Child<ChildInput, ChildRenderModel> internal constructor(
    @PublishedApi internal val context: FormulaContextImpl<*>
) {

    @PublishedApi internal var key: Any? = null
    private var formula: IFormula<ChildInput, ChildRenderModel>? = null

    internal fun initialize(key: Any, formula: IFormula<ChildInput, ChildRenderModel>) {
        if (this.formula != null) {
            throw IllegalStateException("unfinished child definition: ${this.formula}")
        }

        this.key = JoinedKey(key, formula::class)
        this.formula = formula
    }

    internal fun finish() {
        key = null
        formula = null
    }

    /**
     * Use this callback when the child has callbacks and you need to instantiate them in this formula.
     * This will scope callback & event callbacks to child key.
     */
    inline fun input(crossinline create: () -> ChildInput): ChildRenderModel {
        return context.key(checkNotNull(key)) {
            input(create())
        }
    }

    fun input(input: ChildInput): ChildRenderModel {
        val key = checkNotNull(key)
        val formula = checkNotNull(formula)
        val renderModel = context.child(key, formula, input)
        finish()
        return renderModel
    }
}
