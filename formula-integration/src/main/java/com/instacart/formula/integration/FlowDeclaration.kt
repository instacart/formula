package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentContract
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
abstract class FlowDeclaration<FlowComponent> {

    data class Flow<FlowComponent>(
        val bindings: List<Binding<FlowComponent, FragmentContract<*>>>
    )

    /**
     * A utility function to create a binding for [FragmentContract] to the render model management.
     */
    protected fun <State : Any, Contract : FragmentContract<State>> bind(
        type: KClass<Contract>,
        init: (FlowComponent, Contract) -> Flowable<State>
    ): Binding<FlowComponent, FragmentContract<*>> {
        return Binding.single(type, init) as Binding<FlowComponent, FragmentContract<*>>
    }

    protected inline fun <State : Any, reified Contract : FragmentContract<State>> bind(
        noinline init: (FlowComponent, Contract) -> Flowable<State>
    ): Binding<FlowComponent, FragmentContract<*>> {
        return bind(Contract::class, init)
    }

    abstract fun createFlow(): Flow<FlowComponent>
}
