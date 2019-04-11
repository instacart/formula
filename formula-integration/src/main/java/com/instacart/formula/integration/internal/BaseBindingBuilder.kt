package com.instacart.formula.integration.internal

import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.ComponentFactory

/**
 * Base binding builder used to define type safe builders for specific key types. Take a look
 * at [com.instacart.formula.integration.FragmentBindingBuilder].
 */
abstract class BaseBindingBuilder<in ParentComponent, out Component, Key : Any>(
    private val componentFactory: ComponentFactory<ParentComponent, Component>
) {
    private val bindings: MutableList<Binding<Component, Key>> = mutableListOf()

    fun bind(binding: Binding<Component, Key>) = apply {
        bindings.add(binding)
    }

    fun build(): Binding<ParentComponent, Key> {
        return CompositeBinding(componentFactory, bindings)
    }
}
