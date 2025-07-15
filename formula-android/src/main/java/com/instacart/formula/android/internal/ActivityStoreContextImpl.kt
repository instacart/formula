package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.ActivityStoreContext
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable

/**
 * Implementation of [ActivityStoreContext].
 */
internal class ActivityStoreContextImpl<Activity : FragmentActivity> : ActivityStoreContext<Activity>() {

    private val fragmentLifecycleStates = mutableMapOf<String, Lifecycle.State>()
    private val fragmentStateUpdated: PublishRelay<String> = PublishRelay.create()

    private var activity: Activity? = null
    private var hasStarted: Boolean = false

    private val lifecycleStates = BehaviorRelay.createDefault<Lifecycle.State>(Lifecycle.State.INITIALIZED)
    private val activityResultRelay: PublishRelay<ActivityResult> = PublishRelay.create()
    internal val fragmentStateRelay: BehaviorRelay<FragmentState> = BehaviorRelay.create()

    override fun activityLifecycleState(): Observable<Lifecycle.State> = lifecycleStates

    override fun activityResults(): Observable<ActivityResult> = activityResultRelay

    override fun fragmentState(): Observable<FragmentState> = fragmentStateRelay

    override fun isFragmentStarted(tag: String): Observable<Boolean> {
        return fragmentLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
    }

    override fun isFragmentStarted(key: FragmentKey): Observable<Boolean> {
        return isFragmentStarted(key.tag)
    }

    override fun isFragmentResumed(tag: String): Observable<Boolean> {
        return fragmentLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.RESUMED) }
            .distinctUntilChanged()
    }

    override fun isFragmentResumed(key: FragmentKey): Observable<Boolean> {
        return isFragmentResumed(key.tag)
    }

    override fun send(effect: Activity.() -> Unit) {
        // We allow emitting effects only after activity has started
        if (Utils.isMainThread()) {
           startedActivity()?.effect()
        } else {
            Utils.mainThreadHandler.post {
                startedActivity()?.effect()
            }
        }
    }

    fun onLifecycleStateChanged(state: Lifecycle.State) = lifecycleStates.accept(state)

    fun onActivityResult(result: ActivityResult) {
        activityResultRelay.accept(result)
    }

    fun attachActivity(activity: Activity) {
        hasStarted = false
        this.activity = activity
    }

    fun onActivityStarted(activity: Activity) {
        hasStarted = true
    }

    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
        }
    }

    fun updateFragmentLifecycleState(id: FragmentId<*>, newState: Lifecycle.State) {
        // TODO: should probably start using [id] instead of [contract] here.
        val contract = id.key
        if (newState == Lifecycle.State.DESTROYED) {
            fragmentLifecycleStates.remove(contract.tag)
        } else {
            fragmentLifecycleStates[contract.tag] = newState
        }

        fragmentStateUpdated.accept(contract.tag)
    }

    internal fun startedActivity(): Activity? = activity.takeIf { hasStarted }

    private fun fragmentLifecycleState(tag: String): Observable<Lifecycle.State> {
        return fragmentStateUpdated
            .filter { it == tag }
            .startWithItem(tag)
            .map {
                fragmentLifecycleStates[tag] ?: Lifecycle.State.DESTROYED
            }
    }
}
