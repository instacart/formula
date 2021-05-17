package com.instacart.formula.android.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FlowFactory
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentId

/**
 * Defines how specific keys bind to the state management associated
 */
@PublishedApi
internal abstract class Binding<in ParentComponent> {
    companion object {
        fun <ParentComponent, Component> composite(
            flowFactory: FlowFactory<ParentComponent, Component>,
        ): Binding<ParentComponent> {
            return composite(
                flowFactory::createComponent,
                flowFactory.createFlow().bindings
            )
        }

        fun <ParentComponent, Dependencies, Component> composite(
            flowFactory: FlowFactory<Dependencies, Component>,
            toDependencies: (ParentComponent) -> Dependencies,
        ): Binding<ParentComponent> {
            return composite(
                scopeFactory = { component ->
                    val dependencies = toDependencies(component)
                    flowFactory.createComponent(dependencies)
                },
                flowFactory.createFlow().bindings
            )
        }

        @PublishedApi internal fun <ParentComponent, Component> composite(
            scopeFactory: ComponentFactory<ParentComponent, Component>,
            bindings: Bindings<Component>
        ): Binding<ParentComponent> {
            return CompositeBinding(scopeFactory, bindings.types, bindings.bindings)
        }
    }

    data class Input<out Component>(
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
