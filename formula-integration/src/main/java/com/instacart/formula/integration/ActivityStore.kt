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
    internal val onRender: ((A, FragmentFlowState) -> Unit)? = null,
    internal val fragmentFlowStore: FragmentFlowStore
) {

    class Builder<A : FragmentActivity> {
        private var onRender: ((A, FragmentFlowState) -> Unit)? = null


        fun onRenderState(render: (A, FragmentFlowState) -> Unit) {
            onRender = render
        }

        fun build(store: FragmentFlowStore) : ActivityStore<A>  {
            return ActivityStore(onRender, store)
        }

        inline fun build(
            crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
        ): ActivityStore<A> {
            return init(Unit, init)
        }

        inline fun <Component> build(
            rootComponent: Component,
            crossinline init: FragmentBindingBuilder<Component>.() -> Unit
        ) : ActivityStore<A> {
            val fragmentFlowStore = FragmentFlowStore.init(rootComponent, init)
            return build(fragmentFlowStore)
        }
    }

    companion object {

        inline fun <A : FragmentActivity> build(
            crossinline init: Builder<A>.() -> ActivityStore<A>
        ): ActivityStore<A> {
            return Builder<A>().init()
        }

        inline fun <A: FragmentActivity> init(
            crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
        ): ActivityStore<A> {
            return init(Unit, init)
        }

        inline fun <A : FragmentActivity, Component> init(
            rootComponent: Component,
            crossinline init: FragmentBindingBuilder<Component>.() -> Unit
        ): ActivityStore<A> {
            return Builder<A>().build(rootComponent, init)
        }
    }

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        fragmentFlowStore.onLifecycleEffect(event)
    }
}
