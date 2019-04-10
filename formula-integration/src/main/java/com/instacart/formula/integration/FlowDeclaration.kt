package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.integration.internal.SingleBinding
import com.instacart.formula.integration.internal.CompositeBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Defines a Flow, which is a sequence of related screens a user may navigate between to perform a task.
 *
 * @param Input Parent can pass callbacks and initial information when creating a flow.
 * @param ParentComponent A component associated with the parent. Often this will map to the parent dagger component.
 * @param FlowComponent A component that is initialized when user enters this flow and is shared between
 *                  all the screens within the flow. Component will be destroyed when user exists the flow.
 */
abstract class FlowDeclaration<Input, ParentComponent, FlowComponent> {

    data class Flow<ParentComponent, FlowComponent>(
        val flowComponentFactory: (ParentComponent) -> DisposableScope<FlowComponent>,
        val childrenBindings: List<SingleBinding<FragmentContract<*>, FlowComponent, *>>
    ) {

        fun asBinding(): CompositeBinding<ParentComponent, FragmentContract<*>, FlowComponent> {
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
     * A utility function to create a binding for [FragmentContract] to the render model management.
     */
    protected fun <State, Contract : FragmentContract<State>> bind(
        type: KClass<Contract>,
        init: (FlowComponent, Contract) -> Flowable<State>
    ): SingleBinding<FragmentContract<*>, FlowComponent, *> {
        return SingleBinding(
            type.java,
            init
        ) as SingleBinding<FragmentContract<*>, FlowComponent, *>
    }

    abstract fun createFlow(input: Input): Flow<ParentComponent, FlowComponent>

    fun createBinding(input: Input): CompositeBinding<ParentComponent, FragmentContract<*>, FlowComponent> {
        return createFlow(input).asBinding()
    }
}
