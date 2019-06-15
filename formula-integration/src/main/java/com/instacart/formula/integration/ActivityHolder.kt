package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import arrow.core.Option
import arrow.core.toOption
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentLifecycleState
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * This class holds the current instance of the activity of type [Activity].
 */
class ActivityHolder<Activity : FragmentActivity> {
    internal val lifecycleEvents = BehaviorRelay.create<Lifecycle.Event>()
    private val lifecycleEventRelay = PublishRelay.create<Unit>()
    private val startedRelay = PublishRelay.create<Unit>()

    private val fragmentLifecycleStates = mutableMapOf<String, FragmentLifecycleState>()
    private val fragmentStateUpdated: PublishRelay<String> = PublishRelay.create()

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

    fun updateFragmentLifecycleState(contract: FragmentContract<*>, newState: FragmentLifecycleState) {
        // TODO: to avoid leaks, remove the entry from the map when fragment is removed
        fragmentLifecycleStates[contract.tag] = newState
        fragmentStateUpdated.accept(contract.tag)
    }

    fun fragmentLifecycleState(contract: FragmentContract<*>): Observable<FragmentLifecycleState> {
        val key = contract.tag
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
    }
}
