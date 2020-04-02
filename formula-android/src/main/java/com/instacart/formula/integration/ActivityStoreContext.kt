package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

/**
 * This class provides context within which you can create [ActivityStore]. It provides
 * ability to safely communicate with the current activity instance [Activity] and listen for
 * activity events.
 *
 * @param Activity Type of activity that this class provides context for.
 */
abstract class ActivityStoreContext<out Activity : FragmentActivity> {

    // TODO: might be okay to remove in favor of startedActivity
    @PublishedApi internal abstract fun currentActivity(): Activity?

    @PublishedApi internal abstract fun startedActivity(): Activity?

    @PublishedApi internal abstract fun activityAttachEvents(): Observable<Boolean>

    @PublishedApi internal abstract fun activityStartedEvents(): Observable<Unit>

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
        streams: (StreamConfigurator<Activity>.() -> Disposable)? = null,
        contracts: FragmentFlowStore
    ): ActivityStore<ActivityT> {
        val streamStart = streams?.let { streams(it) }

        return ActivityStore(
            contracts =  contracts,
            configureActivity = configureActivity,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            onRenderFragmentState = onRenderFragmentState,
            streams = streamStart
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
        noinline streams: (StreamConfigurator<Activity>.() -> Disposable)? = null,
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
     * Creates [FragmentFlowStore] with a [Component] instance.
     */
    inline fun <Component> contracts(
        rootComponent: Component,
        crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
    ): FragmentFlowStore {
        return FragmentFlowStore.init(rootComponent, contracts)
    }

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    inline fun send(effect: Activity.() -> Unit) {
        // We allow emitting effects only after activity has started
        startedActivity()?.effect() ?: run {
            // Log missing activity.
        }
    }

    /**
     * This enables you to select specific events from the activity.
     *
     * @param Event Type of event
     */
    inline fun <Event> selectActivityEvents(
        crossinline select: Activity.() -> Observable<Event>
    ): Observable<Event> {
        // TODO: should probably use startedActivity
        return activityAttachEvents()
            .switchMap {
                val activity = currentActivity()
                if (activity == null) {
                    Observable.empty<Event>()
                } else {
                    select(activity)
                }
            }
    }

    private fun streams(configure: StreamConfigurator<Activity>.() -> Disposable): () -> Disposable {
        return { StreamConfigurator(this).configure() }
    }

    /**
     * Provides ability to configure RxJava streams that will survive configuration changes.
     */
    class StreamConfigurator<out Activity : FragmentActivity>(
        private val context: ActivityStoreContext<Activity>
    ) {
        /**
         * Keeps activity in-sync with state observable updates. On activity configuration
         * changes, the last update is applied to new activity instance.
         *
         * @param state a state observable
         * @param update an update function
         */
        fun <State> update(state: Observable<State>, update: (Activity, State) -> Unit): Disposable {
            // To keep activity & state in sync, we re-emit state on every activity change.
            val stateEmissions = Observable.combineLatest(
                state,
                context.activityStartedEvents(),
                BiFunction<State, Unit, State> { state, event ->
                    state
                }
            )
            return stateEmissions.subscribe { state ->
                context.startedActivity()?.let {
                    update(it, state)
                }
            }
        }
    }
}
