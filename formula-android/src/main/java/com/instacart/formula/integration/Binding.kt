package com.instacart.formula.integration

import com.instacart.formula.FormulaContext
import com.instacart.formula.integration.internal.CompositeBinding

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<ParentComponent, Key : Any> {
    companion object {
        fun <ParentComponent, Component, Key : Any> composite(
            scopeFactory: ComponentFactory<ParentComponent, Component>,
            bindings: Bindings<Component, Key>
        ): Binding<ParentComponent, Key> {
            return CompositeBinding(scopeFactory, bindings.types, bindings.bindings)
        }
    }

    data class Input<Component, Key : Any>(
        val environment: FlowEnvironment<Key>,
        val component: Component,
        val activeKeys: List<Key>,
        val onStateChanged: (KeyState<Key>) -> Unit
    )


    internal abstract fun types(): Set<Class<*>>

    /**
     * Returns true if this binding handles this [key]
     */
    internal abstract fun binds(key: Any): Boolean

    /**
     * Listens for active key changes and triggers [Input.onStateChanged] events.
     */
    internal abstract fun bind(context: FormulaContext<*>, input: Input<ParentComponent, Key>)
}
