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
     * Returns Flow that emits [FragmentStore] state changes.
     */
    abstract fun fragmentState(): Flow<FragmentState>

    /**
     * Returns Flow that emits true if fragment with a specific [tag] has
     * either [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentStarted(tag: String): Flow<Boolean>

    /**
     * Returns Flow that emits true if fragment with [FragmentKey] has
     * either [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentStarted(key: FragmentKey): Flow<Boolean>

    /**
     * Returns Flow that emits true if fragment with a specific [tag]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentResumed(tag: String): Flow<Boolean>

    /**
     * Returns Flow that emits true if fragment with [FragmentKey]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentResumed(key: FragmentKey): Flow<Boolean>

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    abstract fun send(effect: Activity.() -> Unit)
}
