package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import arrow.core.Option
import arrow.core.toOption
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

/**
 * This class holds the current instance of the activity of type [Activity].
 */
class ActivityHolder<Activity : FragmentActivity> {
    private val lifecycleEventRelay = PublishRelay.create<Unit>()
    private val startedRelay = PublishRelay.create<Unit>()

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
}
