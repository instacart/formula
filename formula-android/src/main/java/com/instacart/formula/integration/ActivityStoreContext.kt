package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.android.StreamConfigurator
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * This class provides context within which you can create [ActivityStore]. It provides
 * ability to safely communicate with the current activity instance [Activity] and listen for
 * activity events.
 *
 * @param Activity Type of activity that this class provides context for.
 */
abstract class ActivityStoreContext<out Activity : FragmentActivity> {

    /**
     * Events for [FragmentActivity.onActivityResult].
     */
    abstract fun activityResults(): Observable<ActivityResult>

    /**
     * Returns RxJava stream that emits Activity lifecycle state [Lifecycle.State].
     */
    abstract fun activityLifecycleState(): Observable<Lifecycle.State>

    /**
     * Returns RxJava stream that emits [FragmentFlowStore] state changes.
     */
    abstract fun fragmentFlowState(): Observable<FragmentFlowState>

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentContract] has either [Lifecycle.State.STARTED]
     * or [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentStarted(contract: FragmentContract<*>): Observable<Boolean>

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentContract]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentResumed(contract: FragmentContract<*>): Observable<Boolean>

    /**
     * This enables you to select specific events from the activity.
     *
     * @param Event Type of event
     */
    abstract fun <Event> selectActivityEvents(
        select: Activity.() -> Observable<Event>
    ): Observable<Event>

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    abstract fun send(effect: Activity.() -> Unit)

    /**
     * Creates an [ActivityStore].
     *
     * @param configureActivity This is called when activity is created before view inflation. You can use this to
     *                          configure / inject the activity.
     * @param onRenderFragmentState This is called after [FragmentFlowState] is applied to UI.
     * @param onFragmentLifecycleEvent This is called after each [FragmentLifecycleEvent].
     * @param streams This provides ability to configure arbitrary RxJava streams that survive
     *                configuration changes. Check [StreamConfigurator] for utility methods.
     * @param contracts [FragmentFlowStore] used to provide state management for individual screens.
     */
    fun <ActivityT : FragmentActivity> store(
        configureActivity: (ActivityT.() -> Unit)? = null,
        onRenderFragmentState: ((ActivityT, FragmentFlowState) -> Unit)? = null,
        onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        streams: (StreamConfigurator<ActivityT>.() -> Disposable)? = null,
        contracts: FragmentFlowStore
    ): ActivityStore<ActivityT> {
        return ActivityStore(
            contracts =  contracts,
            configureActivity = configureActivity,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            onRenderFragmentState = onRenderFragmentState,
            streams = streams
        )
    }

    /**
     * Creates an [ActivityStore].
     *
     * @param configureActivity This is called when activity is created before view inflation. You can use this to
     *                          configure / inject the activity.
     * @param onRenderFragmentState This is called after [FragmentFlowState] is applied to UI.
     * @param onFragmentLifecycleEvent This is called after each [FragmentLifecycleEvent].
     * @param streams This provides ability to configure arbitrary RxJava streams that survive
     *                configuration changes. Check [StreamConfigurator] for utility methods.
     * @param contracts Builder method that configures [FragmentFlowStore] used to provide state management for individual screens.
     */
    inline fun <ActivityT : FragmentActivity> store(
        noinline configureActivity: (ActivityT.() -> Unit)? = null,
        noinline onRenderFragmentState: ((ActivityT, FragmentFlowState) -> Unit)? = null,
        noinline onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        noinline streams: (StreamConfigurator<ActivityT>.() -> Disposable)? = null,
        crossinline contracts: FragmentBindingBuilder<Unit>.() -> Unit = {}
    ): ActivityStore<ActivityT> {
        return store(
            configureActivity = configureActivity,
            onRenderFragmentState = onRenderFragmentState,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            streams = streams,
            contracts =  contracts(Unit, contracts)
        )
    }

    /**
     * Convenience method to to create a [FragmentFlowStore] with a [Component] instance.
     */
    inline fun <Component> contracts(
        rootComponent: Component,
        crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
    ): FragmentFlowStore {
        return FragmentFlowStore.init(rootComponent, contracts)
    }
}
