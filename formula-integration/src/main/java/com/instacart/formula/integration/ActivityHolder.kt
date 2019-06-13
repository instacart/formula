package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import arrow.core.Option
import arrow.core.toOption
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * This class holds the current instance of the activity of type [A].
 */
class ActivityHolder<A : FragmentActivity> {
    private val lifecycleEventRelay = PublishRelay.create<Unit>()
    private val startedRelay = PublishRelay.create<Unit>()

    private var activity: A? = null
    private var hasStarted: Boolean = false

    fun latestActivity(): Observable<Option<A>> {
        return lifecycleEventRelay.startWith(Unit).map {
            activity.toOption()
        }
    }

    fun activityStartedEvents(): Observable<Unit> {
        return startedRelay
    }

    fun attachActivity(activity: A) {
        hasStarted = false
        this.activity = activity
        lifecycleEventRelay.accept(Unit)
    }

    fun onActivityStarted(activity: A) {
        hasStarted = true
        startedRelay.accept(Unit)
    }

    fun detachActivity(activity: A) {
        if (this.activity == activity) {
            this.activity = null
        }
        lifecycleEventRelay.accept(Unit)
    }

    fun currentActivity(): A? {
        return activity
    }

    fun startedActivity(): A? {
        return activity.takeIf { hasStarted }
    }
}
