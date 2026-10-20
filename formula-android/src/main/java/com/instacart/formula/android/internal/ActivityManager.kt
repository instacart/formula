package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.ActivityStore

/**
 * Activity manager connects [ActivityStore] and the [Activity].
 */
internal class ActivityManager<Activity : FragmentActivity>(
    private val delegate: ActivityStoreContextImpl<Activity>,
    private val store: ActivityStore<Activity>
) {

    fun onPreCreate(activity: Activity) {
        // Give store a chance to initialize the activity.
        store.configureActivity?.invoke(activity)
    }

    fun onActivityCreated(activity: Activity) {
        delegate.attachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)
    }

    fun onActivityStarted(activity: Activity) {
        delegate.onActivityStarted(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.STARTED)
    }

    fun onActivityResumed(activity: Activity) {
        delegate.onLifecycleStateChanged(Lifecycle.State.RESUMED)
    }

    fun onActivityPaused(activity: Activity) {
        delegate.onLifecycleStateChanged(Lifecycle.State.STARTED)
    }

    fun onActivityStopped(activity: Activity) {
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)
    }

    fun onActivityDestroyed(activity: Activity) {
        delegate.detachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.DESTROYED)
    }

    fun onActivityResult(result: ActivityResult) {
        delegate.onActivityResult(result)
    }

    fun dispose() {
        store.navigationStore?.dispose()
        store.onCleared?.invoke()
    }
}
