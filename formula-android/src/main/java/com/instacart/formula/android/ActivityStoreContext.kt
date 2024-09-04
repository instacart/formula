package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import io.reactivex.rxjava3.core.Observable

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
    abstract fun activityResults(): Observable<ActivityResult>

    /**
     * Returns RxJava stream that emits Activity lifecycle state [Lifecycle.State].
     */
    abstract fun activityLifecycleState(): Observable<Lifecycle.State>

    /**
     * Returns RxJava stream that emits [FragmentStore] state changes.
     */
    abstract fun fragmentState(): Observable<FragmentState>

    /**
     * Returns RxJava stream that emits true if fragment with a specific [tag] has
     * either [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentStarted(tag: String): Observable<Boolean>

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentKey] has
     * either [Lifecycle.State.STARTED] or [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentStarted(key: FragmentKey): Observable<Boolean>

    /**
     * Returns RxJava stream that emits true if fragment with a specific [tag]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentResumed(tag: String): Observable<Boolean>

    /**
     * Returns RxJava stream that emits true if fragment with [FragmentKey]
     * has [Lifecycle.State.RESUMED] lifecycle state.
     */
    abstract fun isFragmentResumed(key: FragmentKey): Observable<Boolean>

    /**
     * This enables you to select specific events from the activity.
     *
     * @param Event Type of event
     */
    abstract fun <Event : Any> selectActivityEvents(
        select: Activity.() -> Observable<Event>
    ): Observable<Event>

    /**
     * Performs an [effect] on the current activity instance. If there is no activity connected,
     * it will do nothing.
     */
    abstract fun send(effect: Activity.() -> Unit)
}
