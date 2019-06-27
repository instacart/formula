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
        fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
            formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
            input: ChildInput,
            key: FormulaKey,
            onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Effect>
        ): Evaluation<ChildRenderModel>
    }

    override fun callback(wrap: Transition.Factory.() -> Transition<State, Output>): () -> Unit {
        return {
            onChange(wrap(Transition.Factory))
        }
    }

    override fun <UIEvent> eventCallback(wrap: Transition.Factory.(UIEvent) -> Transition<State, Output>): (UIEvent) -> Unit {
        return {
            onChange(wrap(Transition.Factory, it))
        }
    }

    override fun updates(init: FormulaContext.UpdateBuilder<State, Output>.() -> Unit): List<Update> {
        val builder = FormulaContext.UpdateBuilder(onChange)
        builder.init()
        return builder.updates
    }

    override fun <ChildInput, ChildState, ChildOutput, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildOutput, ChildRenderModel>,
        input: ChildInput,
        key: String,
        onEvent: Transition.Factory.(ChildOutput) -> Transition<State, Output>
    ): ChildRenderModel {
        val key = FormulaKey(formula::class, key)
        if (children.containsKey(key)) {
            throw IllegalStateException("There already is a child with same key: $key. Use [key: String] parameter.")
        }

        val result = delegate.child(formula, input, key, onEvent)
        children[key] = result.updates
        return result.renderModel
    }
}
