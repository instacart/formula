package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentContract] to its state
 * management stream.
 */
class ActivityStore<A : FragmentActivity>(
    internal val onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
    internal val fragmentFlowStore: FragmentFlowStore
) {

    class Builder<A : FragmentActivity> {
        private var onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null


        fun onRenderFragmentState(render: (A, FragmentFlowState) -> Unit) {
            onRenderFragmentState = render
        }

        fun build(store: FragmentFlowStore) : ActivityStore<A>  {
            return ActivityStore(onRenderFragmentState, store)
        }

        inline fun build(
            crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
        ): ActivityStore<A> {
            return build(Unit, init)
        }

        inline fun <Component> build(
            rootComponent: Component,
            crossinline init: FragmentBindingBuilder<Component>.() -> Unit
        ) : ActivityStore<A> {
            val fragmentFlowStore = FragmentFlowStore.init(rootComponent, init)
            return build(fragmentFlowStore)
        }
    }

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        fragmentFlowStore.onLifecycleEffect(event)
    }
}
