package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.events.ActivityResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Activity manager connects [ActivityStore] and the [Activity].
 */
@OptIn(DelicateCoroutinesApi::class)
internal class ActivityManager<Activity : FragmentActivity>(
    private val delegate: ActivityStoreContextImpl<Activity>,
    private val store: ActivityStore<Activity>
) {

    internal val stateSubscription = store.navigationStore.state().subscribe { newState ->
        delegate.navigationStateRelay.tryEmit(newState)
    }
    private var fragmentLifecycleTracker: FragmentLifecycleTracker? = null

    fun onPreCreate(activity: Activity) {
        store.configureActivity?.invoke(activity)
        fragmentLifecycleTracker = FragmentLifecycleTracker(
            activity = activity,
            onLifecycleStateChanged = delegate::updateFragmentLifecycleState,
        )
    }

    fun onActivityCreated(activity: Activity) {
        delegate.attachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)

        with(activity) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    delegate.navigationState().collect { state ->
                        store.navigationStore.onPreRenderNavigationState?.invoke(state)
                    }
                }
            }
        }
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
        fragmentLifecycleTracker?.dispose()
        fragmentLifecycleTracker = null

        delegate.detachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.DESTROYED)
    }

    fun onActivityResult(result: ActivityResult) {
        delegate.onActivityResult(result)
    }

    fun onBackPressed(): Boolean = false

    fun dispose() {
        stateSubscription.dispose()
        store.onCleared?.invoke()
    }
}
