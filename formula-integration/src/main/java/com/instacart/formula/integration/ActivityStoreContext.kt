package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

/**
 * This class provides ability to create [ActivityStore]. It provides access to the
 * [ActivityProxy].
 *
 * @param A - type of activity that this class provides context for.
 */
class ActivityStoreContext<A : FragmentActivity>(val proxy: ActivityProxy<A>) {
    private val fragmentFlowStateRelay: BehaviorRelay<FragmentFlowState> = BehaviorRelay.create()

    fun fragmentFlowState(): Observable<FragmentFlowState> {
        return fragmentFlowStateRelay
    }

    /**
     * Creates an [ActivityStore].
     */
    fun store(
        configureActivity: (A.() -> Unit)? = null,
        onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
        onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        contracts: FragmentFlowStore
    ) : ActivityStore<A> {
        return ActivityStore(
            onFragmentFlowStateChanged = fragmentFlowStateRelay::accept,
            proxy = proxy,
            fragmentFlowStore = contracts,
            eventCallbacks = EventCallbacks(
                onInitActivity = configureActivity,
                onFragmentLifecycleEvent = onFragmentLifecycleEvent,
                onRenderFragmentState = onRenderFragmentState
            )
        )
    }

    /**
     * Creates an [ActivityStore].
     */
    inline fun store(
        noinline configureActivity: (A.() -> Unit)? = null,
        noinline onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
        noinline onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        crossinline contracts: FragmentBindingBuilder<Unit>.() -> Unit
    ) : ActivityStore<A> {
        return store(
            configureActivity = configureActivity,
            onRenderFragmentState = onRenderFragmentState,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            contracts = contracts(contracts)
        )
    }


    /**
     * Creates an [ActivityStore].
     */
    inline fun contracts(
        crossinline contracts: FragmentBindingBuilder<Unit>.() -> Unit
    ): FragmentFlowStore {
        return contracts(Unit, contracts)
    }

    /**
     * Creates an [ActivityStore].
     */
    inline fun <Component> contracts(
        rootComponent: Component,
        crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
    ) : FragmentFlowStore {
        return FragmentFlowStore.init(rootComponent, contracts)
    }
}
