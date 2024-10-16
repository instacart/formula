package com.instacart.formula.android.internal

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.ViewFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Activity manager connects [ActivityStore] and the [Activity].
 */
internal class ActivityManager<Activity : FragmentActivity>(
    private val environment: FragmentEnvironment,
    private val delegate: ActivityStoreContextImpl<Activity>,
    private val store: ActivityStore<Activity>
) {

    internal val stateSubscription: Disposable
    private var uiSubscription: Disposable? = null
    private var fragmentRenderView: FragmentFlowRenderView? = null

    init {
        stateSubscription = if (store.streams != null) {
            val disposables = CompositeDisposable()
            disposables.add(subscribeToFragmentStateChanges())
            disposables.add(store.streams.invoke(StreamConfiguratorIml(delegate)))
            disposables
        } else {
            subscribeToFragmentStateChanges()
        }
    }

    fun onPreCreate(activity: Activity) {
        // Give store a chance to initialize the activity.
        store.configureActivity?.invoke(activity)

        // Initialize render view
        fragmentRenderView = FragmentFlowRenderView(
            activity = activity,
            fragmentEnvironment = environment,
            onLifecycleEvent = {
                store.fragmentStore.onLifecycleEffect(it)
                store.onFragmentLifecycleEvent?.invoke(it)
            },
            onLifecycleState = delegate::updateFragmentLifecycleState,
            onFragmentViewStateChanged = store.fragmentStore::onVisibilityChanged
        )
    }

    fun onActivityCreated(activity: Activity) {
        delegate.attachActivity(activity)
        delegate.onLifecycleStateChanged(Lifecycle.State.CREATED)
        val renderView = fragmentRenderView ?: throw callOnPreCreateException(activity)

        uiSubscription = delegate.fragmentState().subscribe {
            renderView.render(it)
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

    fun viewFactory(fragment: FormulaFragment): ViewFactory<Any>? {
        return fragmentRenderView?.viewFactory(fragment)
    }

    private fun subscribeToFragmentStateChanges(): Disposable {
        return store
            .fragmentStore
            .state(environment)
            .subscribe(delegate.fragmentStateRelay::accept)
    }

    private fun callOnPreCreateException(activity: FragmentActivity): IllegalStateException {
        return IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): ${activity::class.java.simpleName}")
    }
}
