package com.instacart.formula.integration.internal

import com.instacart.formula.integration.Binding

/**
 * Base binding builder used to define type safe builders for specific key types. Take a look
 * at [com.instacart.formula.integration.FragmentBindingBuilder].
 */
abstract class BaseBindingBuilder<Component, Key : Any> {
    private val bindings: MutableList<Binding<Component, Key>> = mutableListOf()

    fun bind(binding: Binding<Component, Key>) = apply {
        bindings.add(binding)
    }

    fun build(): List<Binding<Component, Key>> {
        return bindings
    }
}
