package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.ActivityStoreContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Implementation of [ActivityStoreContext].
 */
internal class ActivityStoreContextImpl<Activity : FragmentActivity> : ActivityStoreContext<Activity>() {

    private var activity: Activity? = null
    private var hasStarted: Boolean = false
    private val fragmentLifecycleStates = mutableMapOf<String, Lifecycle.State>()

    private val lifecycleStates = MutableStateFlow(Lifecycle.State.INITIALIZED)

    private val fragmentStateUpdated = MutableSharedFlow<String>(
        extraBufferCapacity = Int.MAX_VALUE,
    )

    private val activityResultRelay = MutableSharedFlow<ActivityResult>(
        extraBufferCapacity = Int.MAX_VALUE,
    )
    internal val fragmentStateRelay = MutableSharedFlow<FragmentState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun activityLifecycleState(): StateFlow<Lifecycle.State> = lifecycleStates

    override fun activityResults(): Flow<ActivityResult> = activityResultRelay

    override fun fragmentState(): Flow<FragmentState> = fragmentStateRelay

    override fun isFragmentStarted(tag: String): Flow<Boolean> {
        return fragmentLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
    }

    override fun isFragmentStarted(key: FragmentKey): Flow<Boolean> {
        return isFragmentStarted(key.tag)
    }

    override fun isFragmentResumed(tag: String): Flow<Boolean> {
        return fragmentLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.RESUMED) }
            .distinctUntilChanged()
    }

    override fun isFragmentResumed(key: FragmentKey): Flow<Boolean> {
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

    fun onLifecycleStateChanged(state: Lifecycle.State) {
        lifecycleStates.tryEmit(state)
    }

    fun onActivityResult(result: ActivityResult) {
        activityResultRelay.tryEmit(result)
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

    fun updateFragmentLifecycleState(id: FragmentId, newState: Lifecycle.State) {
        // TODO: should probably start using [id] instead of [contract] here.
        val contract = id.key
        if (newState == Lifecycle.State.DESTROYED) {
            fragmentLifecycleStates.remove(contract.tag)
        } else {
            fragmentLifecycleStates[contract.tag] = newState
        }

        fragmentStateUpdated.tryEmit(contract.tag)
    }

    internal fun startedActivity(): Activity? = activity.takeIf { hasStarted }

    private fun fragmentLifecycleState(tag: String): Flow<Lifecycle.State> {
        return fragmentStateUpdated
            .filter { it == tag }
            .onStart { emit(tag) }
            .map {
                fragmentLifecycleStates[tag] ?: Lifecycle.State.DESTROYED
            }
    }
}
