package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
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
class ActivityStoreContext<Activity : FragmentActivity>(
    @PublishedApi internal val holder: ActivityHolder<Activity>
) {
    private val fragmentFlowStateRelay: BehaviorRelay<FragmentFlowState> = BehaviorRelay.create()
    private val activityResultRelay: PublishRelay<ActivityResult> = PublishRelay.create()

    internal fun onActivityResult(result: ActivityResult) {
        activityResultRelay.accept(result)
    }

    /**
     * Events for [FragmentActivity.onActivityResult].
     */
    fun activityResults(): Observable<ActivityResult> {
        return activityResultRelay
    }

    /**
     * Returns RxJava stream that emits Activity lifecycle state [Lifecycle.State].
     */
    fun activityLifecycleState(): Observable<Lifecycle.State> {
        return holder.lifecycleStates
    }

    /**
     * Returns RxJava stream that emits [FragmentFlowStore] state changes.
     */
    fun fragmentFlowState(): Observable<FragmentFlowState> {
        return fragmentFlowStateRelay
    }

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentContract] has either [Lifecycle.State.STARTED]
     * or [Lifecycle.State.RESUMED] lifecycle state.
     */
    fun isFragmentStarted(contract: FragmentContract<*>): Observable<Boolean> {
        return holder
            .fragmentLifecycleState(contract)
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
    }

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentContract]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    fun isFragmentResumed(contract: FragmentContract<*>): Observable<Boolean> {
        return holder
            .fragmentLifecycleState(contract)
            .map { it.isAtLeast(Lifecycle.State.RESUMED) }
            .distinctUntilChanged()
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
     * @param contracts [FragmentFlowStore] used to provide state management for individual screens.
     */
    fun store(
        configureActivity: (Activity.() -> Unit)? = null,
        onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
        onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        streams: (StreamConfigurator<Activity>.() -> Disposable)? = null,
        contracts: FragmentFlowStore
    ) : ActivityStore<Activity> {
        val streamStart = createStreamStartFunction(streams)

        return ActivityStore(
            onFragmentFlowStateChanged = fragmentFlowStateRelay::accept,
            context = this,
            fragmentFlowStore = contracts,
            configureActivity = configureActivity,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            onRenderFragmentState = onRenderFragmentState,
            start = streamStart
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
    inline fun store(
        noinline configureActivity: (Activity.() -> Unit)? = null,
        noinline onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
        noinline onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        noinline streams: (StreamConfigurator<Activity>.() -> Disposable)? = null,
        crossinline contracts: FragmentBindingBuilder<Unit>.() -> Unit
    ) : ActivityStore<Activity> {
        return store(
            configureActivity = configureActivity,
            onRenderFragmentState = onRenderFragmentState,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            streams = streams,
            contracts = contracts(Unit, contracts)
        )
    }

    /**
     * Creates [FragmentFlowStore] with a [Component] instance.
     */
    inline fun <Component> contracts(
        rootComponent: Component,
        crossinline contracts: FragmentBindingBuilder<Component>.() -> Unit
    ) : FragmentFlowStore {
        return FragmentFlowStore.init(rootComponent, contracts)
    }

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    inline fun send(effect: Activity.() -> Unit) {
        // We allow emitting effects only after activity has started
        holder.startedActivity()?.effect() ?: run {
            // Log missing activity.
        }
    }

    /**
     * This enables you to select specific events from the activity.
     *
     * @param Event Type of event
     */
    inline fun <Event> selectActivityEvents(crossinline select: Activity.() -> Observable<Event>): Observable<Event> {
        return holder.latestActivity().switchMap {
            val activity = it.orNull()
            if (activity == null) {
                Observable.empty<Event>()
            } else {
                select(activity)
            }
        }
    }

    private fun createStreamStartFunction(
        stream: (StreamConfigurator<Activity>.() -> Disposable)?
    ): (() -> Disposable)? {
        return stream?.let { configure ->
            {
                StreamConfigurator(this).configure()
            }
        }
    }

    /**
     * Provides ability to configure RxJava streams that will survive configuration changes.
     */
    class StreamConfigurator<Activity : FragmentActivity>(private val context: ActivityStoreContext<Activity>) {

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
                context.holder.activityStartedEvents(),
                BiFunction<State, Unit, State> { state, event ->
                    state
                }
            )
            return stateEmissions.subscribe { state ->
                context.holder.currentActivity()?.let {
                    update(it, state)
                }
            }
        }
    }
}
