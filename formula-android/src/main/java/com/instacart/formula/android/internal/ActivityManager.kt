package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.ActivityStore
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

    internal val stateSubscription = store.fragmentStore.state().subscribe { newState ->
        delegate.fragmentStateRelay.tryEmit(newState)
    }
    private var fragmentRenderView: FragmentFlowRenderView? = null

    fun onPreCreate(activity: Activity) {
        // Give store a chance to initialize the activity.
        store.configureActivity?.invoke(activity)

        // Initialize render view
        fragmentRenderView = FragmentFlowRenderView(
            activity = activity,
            store = store.fragmentStore,
            onLifecycleState = delegate::updateFragmentLifecycleState,
            onFragmentViewStateChanged = store.fragmentStore::onVisibilityChanged
        )
    }

    fun onActivityCreated(activity: Activity) {
        delegate.attachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)

        val renderView = fragmentRenderView ?: throw callOnPreCreateException(activity)
        with(activity) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    delegate.fragmentState().collect {
                        renderView.render(it)
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
        fragmentRenderView = null

        delegate.detachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.DESTROYED)
    }

    fun onActivityResult(result: ActivityResult) {
        delegate.onActivityResult(result)
    }

    fun onBackPressed(): Boolean {
        return fragmentRenderView?.onBackPressed() ?: false
    }

    fun dispose() {
        stateSubscription.dispose()
        store.onCleared?.invoke()
    }

    private fun callOnPreCreateException(activity: FragmentActivity): IllegalStateException {
        return IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): ${activity::class.java.simpleName}")
    }
}
