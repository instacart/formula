package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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
    abstract fun activityResults(): Flow<ActivityResult>

    /**
     * Returns Flow that emits Activity lifecycle state [Lifecycle.State].
     */
    abstract fun activityLifecycleState(): StateFlow<Lifecycle.State>

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    abstract fun send(effect: Activity.() -> Unit)
}
