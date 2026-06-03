package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.NavigationState
import com.instacart.formula.android.RouteKey
import com.instacart.formula.android.RouteId
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
    private val routeLifecycleStates = mutableMapOf<String, Lifecycle.State>()

    private val lifecycleStates = MutableStateFlow(Lifecycle.State.INITIALIZED)

    private val routeStateUpdated = MutableSharedFlow<String>(
        extraBufferCapacity = Int.MAX_VALUE,
    )

    private val activityResultRelay = MutableSharedFlow<ActivityResult>(
        extraBufferCapacity = Int.MAX_VALUE,
    )
    internal val navigationStateRelay = MutableSharedFlow<NavigationState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun activityLifecycleState(): StateFlow<Lifecycle.State> = lifecycleStates

    override fun activityResults(): Flow<ActivityResult> = activityResultRelay

    override fun navigationState(): Flow<NavigationState> = navigationStateRelay

    override fun isRouteStarted(tag: String): Flow<Boolean> {
        return routeLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
    }

    override fun isRouteStarted(key: RouteKey): Flow<Boolean> {
        return isRouteStarted(key.tag)
    }

    override fun isRouteResumed(tag: String): Flow<Boolean> {
        return routeLifecycleState(tag)
            .map { it.isAtLeast(Lifecycle.State.RESUMED) }
            .distinctUntilChanged()
    }

    override fun isRouteResumed(key: RouteKey): Flow<Boolean> {
        return isRouteResumed(key.tag)
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
        clearRouteLifecycleState()
    }

    fun onActivityStarted(activity: Activity) {
        hasStarted = true
    }

    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
        }
    }

    fun updateRouteLifecycleState(id: RouteId<*>, newState: Lifecycle.State) {
        // TODO: should probably start using [id] instead of [contract] here.
        val contract = id.key
        if (newState == Lifecycle.State.DESTROYED) {
            routeLifecycleStates.remove(contract.tag)
        } else {
            routeLifecycleStates[contract.tag] = newState
        }

        routeStateUpdated.tryEmit(contract.tag)
    }

    /**
     * Clears all stored route lifecycle state. Route lifecycle state is tied to the activity/view
     * instance rather than durable store state, so it must be reset when the activity is destroyed.
     *
     * The Fragment host self-heals via [NavigationFlowRenderView.onFragmentViewDestroyed], but a
     * non-Fragment host (e.g. Compose Nav 3) has no such teardown. Since [ActivityStoreContextImpl]
     * is reused across configuration changes (see [AppManager]), without this the delegate would
     * retain stale STARTED/RESUMED entries from the previous activity instance.
     */
    fun clearRouteLifecycleState() {
        if (routeLifecycleStates.isEmpty()) return

        val tags = routeLifecycleStates.keys.toList()
        routeLifecycleStates.clear()
        // Notify any active collectors so they re-read the (now absent) state.
        tags.forEach(routeStateUpdated::tryEmit)
    }

    internal fun startedActivity(): Activity? = activity.takeIf { hasStarted }

    private fun routeLifecycleState(tag: String): Flow<Lifecycle.State> {
        return routeStateUpdated
            .filter { it == tag }
            .onStart { emit(tag) }
            .map {
                routeLifecycleStates[tag] ?: Lifecycle.State.DESTROYED
            }
    }
}
