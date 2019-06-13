package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.activity.ActivityResult
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
 * @param Activity - type of activity that this class provides context for.
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

    fun fragmentFlowState(): Observable<FragmentFlowState> {
        return fragmentFlowStateRelay
    }

    /**
     * Creates an [ActivityStore].
     */
    fun store(
        configureActivity: (Activity.() -> Unit)? = null,
        onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
        onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        start: (() -> Disposable)? = null,
        contracts: FragmentFlowStore
    ) : ActivityStore<Activity> {
        return ActivityStore(
            onFragmentFlowStateChanged = fragmentFlowStateRelay::accept,
            context = this,
            fragmentFlowStore = contracts,
            configureActivity = configureActivity,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            onRenderFragmentState = onRenderFragmentState,
            start = start
        )
    }

    /**
     * Creates an [ActivityStore].
     */
    inline fun store(
        noinline configureActivity: (Activity.() -> Unit)? = null,
        noinline onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
        noinline onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
        noinline start: (() -> Disposable)? = null,
        crossinline contracts: FragmentBindingBuilder<Unit>.() -> Unit
    ) : ActivityStore<Activity> {
        return store(
            configureActivity = configureActivity,
            onRenderFragmentState = onRenderFragmentState,
            onFragmentLifecycleEvent = onFragmentLifecycleEvent,
            start = start,
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
     * Keeps activity in-sync with state observable updates. On activity configuration
     * changes, the last update is applied to new activity instance.
     *
     * @param state - a state observable
     * @param update - an update function
     */
    fun <State> update(state: Observable<State>, update: (Activity, State) -> Unit): Disposable {
        // To keep activity & state in sync, we re-emit state on every activity change.
        val stateEmissions = Observable.combineLatest(
            state,
            holder.activityStartedEvents(),
            BiFunction<State, Unit, State> { state, event ->
                state
            }
        )
        return stateEmissions.subscribe { state ->
            holder.currentActivity()?.let {
                update(it, state)
            }
        }
    }

    /**
     * This enables you to select specific events from the activity.
     *
     * [Event] - type of event
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
}
