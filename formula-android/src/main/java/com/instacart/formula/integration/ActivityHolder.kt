package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import arrow.core.Option
import arrow.core.toOption
import com.instacart.formula.fragment.FragmentContract
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * This class holds the current instance of the activity of type [Activity].
 */
class ActivityHolder<Activity : FragmentActivity> {
    internal val lifecycleStates = BehaviorRelay.createDefault<Lifecycle.State>(Lifecycle.State.INITIALIZED)
    private val lifecycleEventRelay = PublishRelay.create<Unit>()
    private val startedRelay = PublishRelay.create<Unit>()

    private val fragmentLifecycleStates = mutableMapOf<String, Lifecycle.Event>()
    private val fragmentStateUpdated: PublishRelay<String> = PublishRelay.create()
    private val fragmentDestroyed: PublishRelay<String> = PublishRelay.create()

    private var activity: Activity? = null
    private var hasStarted: Boolean = false

    fun latestActivity(): Observable<Option<Activity>> {
        return lifecycleEventRelay.startWith(Unit).map {
            activity.toOption()
        }
    }

    fun activityStartedEvents(): Observable<Unit> {
        return startedRelay
    }

    fun attachActivity(activity: Activity) {
        hasStarted = false
        this.activity = activity
        lifecycleEventRelay.accept(Unit)
    }

    fun onActivityStarted(activity: Activity) {
        hasStarted = true
        startedRelay.accept(Unit)
    }

    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
        }
        lifecycleEventRelay.accept(Unit)
    }

    fun currentActivity(): Activity? {
        return activity
    }

    fun startedActivity(): Activity? {
        return activity.takeIf { hasStarted }
    }

    fun updateFragmentLifecycleState(contract: FragmentContract<*>, newState: Lifecycle.Event) {
        if (newState == Lifecycle.Event.ON_DESTROY) {
            fragmentLifecycleStates.remove(contract.tag)
            fragmentDestroyed.accept(contract.tag)
        } else {
            fragmentLifecycleStates[contract.tag] = newState
            fragmentStateUpdated.accept(contract.tag)
        }
    }

    fun fragmentLifecycleState(contract: FragmentContract<*>): Observable<Lifecycle.Event> {
        val key = contract.tag
        val destroyedEvents = fragmentDestroyed.filter { it == key }.map { Lifecycle.Event.ON_DESTROY }
        return fragmentStateUpdated
            .filter { it == key }
            .startWith(key)
            .flatMap {
                val state = fragmentLifecycleStates[key]
                if (state == null) {
                    Observable.empty()
                } else {
                    Observable.just(state)
                }
            }
            .mergeWith(destroyedEvents)
    }
}
