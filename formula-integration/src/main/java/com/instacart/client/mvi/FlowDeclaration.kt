package com.instacart.client.mvi

import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Defines a Flow, which is a sequence of related screens a user may navigate between to perform a task.
 *
 * [Input] - Parent can pass callbacks and initial information when creating a flow.
 * [ParentComponent] - A component associated with the parent. Often this will map to the parent dagger component.
 * [FlowComponent] - A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
abstract class FlowDeclaration<Input, ParentComponent, FlowComponent> {

    data class Flow<ParentComponent, FlowComponent>(
        val flowComponentFactory: (ParentComponent) -> DisposableScope<FlowComponent>,
        val childrenBindings: List<ICMviBinding.Binding<ICMviFragmentContract<*>, FlowComponent, *>>
    ) {
        fun asMviBinding(): ICMviBinding.CompositeBinding<ParentComponent, ICMviFragmentContract<*>, FlowComponent> {
            return ICMviBinding
                .Builder<ParentComponent, FlowComponent, ICMviFragmentContract<*>>(flowComponentFactory)
                .apply {
                    childrenBindings.forEach {
                        bind(it)
                    }
                }
                .build()
        }
    }

    /**
     * Helper function to create an mvi binding
     */
    protected fun <State, Contract : ICMviFragmentContract<State>> bind(
        type: KClass<Contract>,
        init: (FlowComponent, Contract) -> Flowable<State>
    ): ICMviBinding.Binding<ICMviFragmentContract<*>, FlowComponent, *> {
        return ICMviBinding.Binding(type.java, init) as ICMviBinding.Binding<ICMviFragmentContract<*>, FlowComponent, *>
    }

    abstract fun createFlow(input: Input): Flow<ParentComponent, FlowComponent>

    fun createBinding(input: Input): ICMviBinding.CompositeBinding<ParentComponent, ICMviFragmentContract<*>, FlowComponent> {
        return createFlow(input).asMviBinding()
    }
}
