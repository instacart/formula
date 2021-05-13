package com.instacart.formula.android

import com.instacart.formula.android.internal.Bindings

/**
 * A flow defines a group of features that share a same [FlowComponent].
 */
data class Flow<FlowComponent> @PublishedApi internal constructor(
    internal val bindings: Bindings<FlowComponent>
) {
    companion object {
        /**
         * Utility function to build a flow.
         */
        inline fun <FlowComponent> build(
            crossinline init: FragmentBindingBuilder<FlowComponent>.() -> Unit
        ): Flow<FlowComponent> {
            val bindings = FragmentBindingBuilder.build(init)
            return Flow(bindings)
        }
    }
}