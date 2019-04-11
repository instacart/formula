package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract

/**
 * Defines integration of a flow.
 *
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param FlowComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
abstract class FlowIntegration<in ParentComponent, FlowComponent> {

    abstract val flowDeclaration: FlowDeclaration<FlowComponent>

    abstract fun createComponent(parentComponent: ParentComponent): DisposableScope<FlowComponent>

    fun binding(): Binding<ParentComponent, FragmentContract<*>> {
        return Binding.composite(this::createComponent, flowDeclaration.createFlow().bindings)
    }
}
