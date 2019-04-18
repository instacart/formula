package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract

/**
 * The FlowDeclaration class defines a [Flow], which is a sequence of related screens a user may navigate
 * between to perform a task. A shared [FlowComponent] is passed to individual screen integrations to help
 * initialize their render model stream. This component can be used to share state, routers, action handlers
 * and other dependencies.
 *
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
    protected inline fun build(crossinline init: FragmentBindingBuilder<FlowComponent>.() -> Unit): Flow<FlowComponent> {
        val bindings = FragmentBindingBuilder.build(init)
        return Flow(bindings)
    }

    abstract fun createFlow(): Flow<FlowComponent>
}
