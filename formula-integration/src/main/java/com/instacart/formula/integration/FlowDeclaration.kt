package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract

/**
 * Defines a Flow, which is a sequence of related screens a user may navigate between to perform a task.
 *
 * @param Input Parent can pass callbacks and initial information when creating a flow.
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param FlowComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
abstract class FlowDeclaration<FlowComponent> {

    data class Flow<FlowComponent>(
        val bindings: List<Binding<FlowComponent, FragmentContract<*>>>
    )

    /**
     * A utility function to build a flow.
     */
    protected inline fun build(init: FragmentBindingBuilder<FlowComponent>.() -> Unit): Flow<FlowComponent> {
        val bindings = FragmentBindingBuilder<FlowComponent>().apply(init).build()
        return Flow(bindings)
    }

    abstract fun createFlow(): Flow<FlowComponent>
}
