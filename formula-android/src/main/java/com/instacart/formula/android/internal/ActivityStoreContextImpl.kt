package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.integration.ActivityStoreContext
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

internal class ActivityStoreContextImpl<Activity : FragmentActivity> : ActivityStoreContext<Activity>() {

    private val attachEventRelay = BehaviorRelay.createDefault(false)
    private val startedRelay = PublishRelay.create<Unit>()

    private val fragmentLifecycleStates = mutableMapOf<String, Lifecycle.State>()
    private val fragmentStateUpdated: PublishRelay<String> = PublishRelay.create()

    private var activity: Activity? = null
    private var hasStarted: Boolean = false

    private val lifecycleStates = BehaviorRelay.createDefault<Lifecycle.State>(Lifecycle.State.INITIALIZED)
    private val activityResultRelay: PublishRelay<ActivityResult> = PublishRelay.create()
    internal val fragmentFlowStateRelay: BehaviorRelay<FragmentFlowState> = BehaviorRelay.create()

    override fun currentActivity(): Activity? = activity

    override fun startedActivity(): Activity? = activity.takeIf { hasStarted }

    override fun activityAttachEvents(): Observable<Boolean> = attachEventRelay

    override fun activityStartedEvents(): Observable<Unit> = startedRelay

    override fun activityLifecycleState(): Observable<Lifecycle.State> = lifecycleStates

    override fun activityResults(): Observable<ActivityResult> = activityResultRelay

    override fun fragmentFlowState(): Observable<FragmentFlowState> = fragmentFlowStateRelay

    override fun isFragmentStarted(contract: FragmentContract<*>): Observable<Boolean> {
        return fragmentLifecycleState(contract)
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
    }

    override fun isFragmentResumed(contract: FragmentContract<*>): Observable<Boolean> {
        return fragmentLifecycleState(contract)
            .map { it.isAtLeast(Lifecycle.State.RESUMED) }
            .distinctUntilChanged()
    }

    fun onLifecycleStateChanged(state: Lifecycle.State) = lifecycleStates.accept(state)

    fun onActivityResult(result: ActivityResult) {
        activityResultRelay.accept(result)
    }

    fun attachActivity(activity: Activity) {
        hasStarted = false
        this.activity = activity
        attachEventRelay.accept(true)
    }

    fun onActivityStarted(activity: Activity) {
        hasStarted = true
        startedRelay.accept(Unit)
    }

    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
        }
        attachEventRelay.accept(false)
    }

    fun updateFragmentLifecycleState(contract: FragmentContract<*>, newState: Lifecycle.State) {
        if (newState == Lifecycle.State.DESTROYED) {
            fragmentLifecycleStates.remove(contract.tag)
        } else {
            fragmentLifecycleStates[contract.tag] = newState
        }

        fragmentStateUpdated.accept(contract.tag)
    }

    private fun fragmentLifecycleState(contract: FragmentContract<*>): Observable<Lifecycle.State> {
        val key = contract.tag
        return fragmentStateUpdated
            .filter { it == key }
            .startWith(key)
            .map {
                fragmentLifecycleStates[key] ?: Lifecycle.State.DESTROYED
            }
    }
}