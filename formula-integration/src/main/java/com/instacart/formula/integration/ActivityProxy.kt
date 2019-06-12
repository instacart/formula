package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import arrow.core.Option
import arrow.core.toOption
import com.instacart.formula.activity.ActivityResult
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction

/**
 * A proxy for two-way communication with an activity. Since, activity can
 * be re-created during configuration changes, this proxy makes sure
 * you are communicating with the right activity instance.
 *
 * @param A - type of Activity.
 */
class ActivityProxy<A : FragmentActivity> {
    // Event relays
    private val lifecycleEventRelay = PublishRelay.create<Unit>()
    private val activityResultRelay: PublishRelay<ActivityResult> = PublishRelay.create()

    @PublishedApi internal var activity: A? = null

    @PublishedApi internal fun latestActivity(): Observable<Option<A>> {
        return lifecycleEventRelay.startWith(Unit).map {
            activity.toOption()
        }
    }

    internal fun attachActivity(activity: A) {
        this.activity = activity
        lifecycleEventRelay.accept(Unit)
    }

    internal fun detachActivity(activity: A) {
        if (this.activity == activity) {
            this.activity = null
        }
        lifecycleEventRelay.accept(Unit)
    }

    internal fun onActivityResult(result: ActivityResult) {
        activityResultRelay.accept(result)
    }

    inline fun send(effect: A.() -> Unit) {
        activity?.effect() ?: run {
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
    fun <State> update(state: Observable<State>, update: (A, State) -> Unit): Disposable {
        // To keep activity & state in sync, we re-emit state on every activity change.
        val stateEmissions = Observable.combineLatest(
            state,
            latestActivity(),
            BiFunction<State, Option<A>, State> { state, activity ->
                state
            }
        )
        return stateEmissions.subscribe { state ->
            activity?.let {
                update(it, state)
            }
        }
    }

    /**
     * Events for [FragmentActivity.onActivityResult].
     */
    fun activityResults(): Observable<ActivityResult> {
        return activityResultRelay
    }

    /**
     * This enables you to select specific events from the activity.
     *
     * [Event] - type of event
     */
    inline fun <Event> selectActivityEvents(crossinline select: A.() -> Observable<Event>): Observable<Event> {
        return latestActivity().switchMap {
            val activity = it.orNull()
            if (activity == null) {
                Observable.empty<Event>()
            } else {
                select(activity)
            }
        }
    }
}
