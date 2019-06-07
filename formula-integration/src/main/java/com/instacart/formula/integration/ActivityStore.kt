package com.instacart.formula.integration

import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentContract] to its state
 * management stream.
 */
class ActivityStore(
    val fragmentFlowStore: FragmentFlowStore
) {

    companion object {
        inline fun init(crossinline init: FragmentBindingBuilder<Unit>.() -> Unit): ActivityStore {
            return init(Unit, init)
        }

        inline fun <Component> init(
            rootComponent: Component,
            crossinline init: FragmentBindingBuilder<Component>.() -> Unit
        ): ActivityStore {
            val fragmentFlowStore = FragmentFlowStore.init(rootComponent, init)
            return ActivityStore(fragmentFlowStore)
        }
    }

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        fragmentFlowStore.onLifecycleEffect(event)
    }
}
