package com.instacart.formula.integration

import com.instacart.formula.FormulaContext
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.integration.internal.CompositeBinding

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<ParentComponent> {
    companion object {
        fun <ParentComponent, Component> composite(
            scopeFactory: ComponentFactory<ParentComponent, Component>,
            bindings: Bindings<Component>
        ): Binding<ParentComponent> {
            return CompositeBinding(scopeFactory, bindings.types, bindings.bindings)
        }
    }

    data class Input<Component>(
        val environment: FragmentEnvironment,
        val component: Component,
        val activeKeys: List<FragmentId>,
        val onInitializeFeature: (FeatureEvent) -> Unit,
    )

    internal abstract fun types(): Set<Class<*>>

    /**
     * Returns true if this binding handles this [key]
     */
    internal abstract fun binds(key: Any): Boolean

    /**
     * Listens for active key changes and triggers [Input.onStateChanged] events.
     */
    internal abstract fun bind(context: FormulaContext<*>, input: Input<ParentComponent>)
}
