package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore

/**
 * This class provides ability to create [ActivityStore]. It provides access to the
 * [ActivityProxy].
 */
class ActivityStoreContext<A : FragmentActivity>(val proxy: ActivityProxy<A>) {

    fun build(
        onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
        store: FragmentFlowStore
    ) : ActivityStore<A>  {
        return ActivityStore(onRenderFragmentState, proxy, store)
    }

    inline fun build(
        noinline onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
        crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
    ): ActivityStore<A> {
        return build(Unit, onRenderFragmentState, init)
    }

    inline fun <Component> build(
        rootComponent: Component,
        noinline onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
        crossinline init: FragmentBindingBuilder<Component>.() -> Unit
    ) : ActivityStore<A> {
        val fragmentFlowStore = FragmentFlowStore.init(rootComponent, init)
        return build(onRenderFragmentState, fragmentFlowStore)
    }
}
