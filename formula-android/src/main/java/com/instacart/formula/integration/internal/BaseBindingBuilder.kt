package com.instacart.formula.integration.internal

import com.instacart.formula.integration.Binding
import com.instacart.formula.integration.Bindings
import java.lang.IllegalStateException

/**
 * Base binding builder used to define type safe builders for specific key types. Take a look
 * at [com.instacart.formula.integration.FragmentBindingBuilder].
 */
abstract class BaseBindingBuilder<Component> {
    private val types = mutableSetOf<Class<*>>()
    private val bindings: MutableList<Binding<Component>> = mutableListOf()

    internal fun bind(binding: Binding<Component>) = apply {
        binding.types().forEach {
            if (types.contains(it)) {
                throw IllegalStateException("Binding for $it already exists")
            }
            types += it
        }

        bindings += binding
    }

    fun build(): Bindings<Component> {
        return Bindings(
            types = types,
            bindings = bindings
        )
    }
}
