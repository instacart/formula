package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.integration.ActivityStore
import com.instacart.formula.integration.FragmentFlowRenderView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Activity manager connects [ActivityStore] and the [Activity].
 */
internal class ActivityManager<Activity : FragmentActivity>(
    private val environment: FragmentEnvironment,
    private val delegate: ActivityStoreContextImpl<Activity>,
    private val store: ActivityStore<Activity>
) {

    private val fragmentState = store
        .fragments
        .state(environment)
        .doOnNext(delegate.fragmentFlowStateRelay::accept)
        .replay(1)

    internal val stateSubscription: Disposable
    private var uiSubscription: Disposable? = null
    private var fragmentRenderView: FragmentFlowRenderView? = null

    init {
        stateSubscription = if (store.streams != null) {
            val disposables = CompositeDisposable()
            disposables.add(fragmentState.connect())
            disposables.add(store.streams.invoke())
            disposables
        } else {
            fragmentState.connect()
        }
    }

    fun onPreCreate(activity: Activity) {
        // Give store a chance to initialize the activity.
        store.configureActivity?.invoke(activity)

        // Initialize render view
        fragmentRenderView = FragmentFlowRenderView(
            activity = activity,
            onLifecycleEvent = {
                store.fragments.onLifecycleEffect(it)
                store.onFragmentLifecycleEvent?.invoke(it)
            },
            onLifecycleState = delegate::updateFragmentLifecycleState,
            onFragmentViewStateChanged = store.fragments::onVisibilityChanged
        )
    }

    fun onActivityCreated(activity: Activity) {
        delegate.attachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)
        val renderView = fragmentRenderView ?: throw callOnPreCreateException(activity)
        uiSubscription = fragmentState.subscribe {
            renderView.renderer.render(it)
            store.onRenderFragmentState?.invoke(activity, it)
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
        uiSubscription?.dispose()
        uiSubscription = null

        fragmentRenderView?.dispose()
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
    }

    private fun callOnPreCreateException(activity: FragmentActivity): IllegalStateException {
        return IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): ${activity::class.java.simpleName}")
    }
}