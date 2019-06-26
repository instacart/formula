package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.ProcessorFormula
import com.instacart.formula.Transition
import com.instacart.formula.StreamConnection
import java.lang.IllegalStateException

class RealRxFormulaContext<State, Effect>(
    private val delegate: Delegate<State, Effect>,
    private val onChange: (Transition<State, Effect>) -> Unit
) : FormulaContext<State, Effect> {

    var children = mutableMapOf<FormulaKey, List<StreamConnection<*, *>>>()

    interface Delegate<State, Effect> {
        fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
            formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEffect: (ChildEffect) -> Transition<State, Effect>
        ): Evaluation<ChildRenderModel>
    }

    override fun transition(state: State) {
        onChange(Transition(state))
    }

    override fun transition(state: State, effect: Effect?) {
        onChange(Transition(state, effect))
    }

    override fun updates(init: FormulaContext.UpdateBuilder<State, Effect>.() -> Unit): List<StreamConnection<*, *>> {
        val builder = FormulaContext.UpdateBuilder(onChange)
        builder.init()
        return builder.updates
    }

    override fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: String,
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel {
        val key = FormulaKey(formula::class, key)
        if (children.containsKey(key)) {
            throw IllegalStateException("There already is a child with same key: $key. Use [key: String] parameter.")
        }

        val result = delegate.child(formula, input, key, onEffect)
        children[key] = result.updates
        return result.renderModel
    }
}
