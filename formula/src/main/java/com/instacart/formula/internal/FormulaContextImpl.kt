package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Transition
import com.instacart.formula.Update
import java.lang.IllegalStateException

class FormulaContextImpl<State, Output>(
    private val delegate: Delegate<State, Output>,
    private val onChange: (Transition<State, Output>) -> Unit
) : FormulaContext<State, Output> {

    var children = mutableMapOf<FormulaKey, List<Update>>()

    interface Delegate<State, Effect> {
        fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEffect: Transition.Factory.(ChildEffect) -> Transition<State, Effect>
        ): Evaluation<ChildRenderModel>
    }

    override fun transition(state: State) {
        onChange(Transition.Factory.transition(state))
    }

    override fun transition(state: State, output: Output?) {
        onChange(Transition.Factory.transition(state, output))
    }

    override fun output(output: Output) {
        onChange(Transition.Factory.output(output))
    }

    override fun updates(init: FormulaContext.UpdateBuilder<State, Output>.() -> Unit): List<Update> {
        val builder = FormulaContext.UpdateBuilder(onChange)
        builder.init()
        return builder.updates
    }

    override fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: String,
        onEffect: Transition.Factory.(ChildEffect) -> Transition<State, Output>
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
