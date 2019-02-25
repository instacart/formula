package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
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
        val childrenBindings: List<KeyBinding.Binding<FragmentContract<*>, FlowComponent, *>>
    ) {
        fun asMviBinding(): KeyBinding.CompositeBinding<ParentComponent, FragmentContract<*>, FlowComponent> {
            return KeyBinding.Builder<ParentComponent, FlowComponent, FragmentContract<*>>(
                flowComponentFactory
            )
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
    protected fun <State, Contract : FragmentContract<State>> bind(
        type: KClass<Contract>,
        init: (FlowComponent, Contract) -> Flowable<State>
    ): KeyBinding.Binding<FragmentContract<*>, FlowComponent, *> {
        return KeyBinding.Binding(
            type.java,
            init
        ) as KeyBinding.Binding<FragmentContract<*>, FlowComponent, *>
    }

    abstract fun createFlow(input: Input): Flow<ParentComponent, FlowComponent>

    fun createBinding(input: Input): KeyBinding.CompositeBinding<ParentComponent, FragmentContract<*>, FlowComponent> {
        return createFlow(input).asMviBinding()
    }
}
