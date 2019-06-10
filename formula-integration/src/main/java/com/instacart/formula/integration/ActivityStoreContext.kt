package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore

/**
 * This class provides ability to create [ActivityStore]. It provides access to the
 * [ActivityProxy].
 *
 * @param A - type of activity that this class provides context for.
 */
class ActivityStoreContext<A : FragmentActivity>(val proxy: ActivityProxy<A>) {

    /**
     * Creates an [ActivityStore].
     */
    fun store(
        eventCallbacks: EventCallbacks<A>? = null,
        store: FragmentFlowStore
    ) : ActivityStore<A>  {
        return ActivityStore(proxy, store, eventCallbacks ?: EventCallbacks())
    }

    /**
     * Creates an [ActivityStore].
     */
    inline fun store(
        eventCallbacks: EventCallbacks<A>? = null,
        crossinline init: FragmentBindingBuilder<Unit>.() -> Unit
    ): ActivityStore<A> {
        return store(Unit, eventCallbacks, init)
    }

    /**
     * Creates an [ActivityStore].
     */
    inline fun <Component> store(
        rootComponent: Component,
        eventCallbacks: EventCallbacks<A>? = null,
        crossinline init: FragmentBindingBuilder<Component>.() -> Unit
    ) : ActivityStore<A> {
        val fragmentFlowStore = FragmentFlowStore.init(rootComponent, init)
        return store(eventCallbacks, fragmentFlowStore)
    }
}
