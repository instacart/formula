package com.instacart.formula.integration

/**
 *
 * The flow integration class defines how a parent should integrate a particular flow. This class takes a
 * [ParentComponent] and creates a [FlowComponent] that will be shared between all the flows sub-integrations.
 *
 * ```kotlin
 * class MyFlowIntegration : FlowIntegration<AppComponent, MyFlowComponent>() {
 *   override val flowDeclaration = MyFlowDeclaration()
 *
 *   override fun createComponent(parentComponent: AppComponent): DisposableScope<MyFlowComponent> {
 *     val flowComponent = parentComponent.createMyFlowComponent()
 *     flowComponent.initialize()
 *     return DisposableScope(component) {
 *       flowComponent.dispose()
 *     }
 *   }
 * }
 * ```
 *
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param FlowComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 * @see FlowDeclaration
 */
abstract class FlowIntegration<ParentComponent, FlowComponent> {

    protected abstract val flowDeclaration: FlowDeclaration<FlowComponent>

    protected abstract fun createComponent(parentComponent: ParentComponent): DisposableScope<FlowComponent>

    fun binding(): Binding<ParentComponent> {
        return Binding.composite(this::createComponent, flowDeclaration.createFlow().bindings)
    }
}
