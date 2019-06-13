package com.instacart.formula.integration

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.fragment.FragmentFlowStore
import io.reactivex.disposables.Disposable
import java.lang.IllegalStateException
import java.util.UUID

/**
 * Manages [FragmentFlowStore] for individual activities. [FragmentFlowStore] survives configuration changes
 * by default.
 */
internal class StoreManager(
    private val factory: AppStoreFactory
) {

    companion object {
        private const val BUNDLE_KEY = "formula::activity::key"
    }

    private val activityToKeyMap = mutableMapOf<Activity, String>()
    private val componentMap = mutableMapOf<String, ActivityStore<FragmentActivity>>()

    private val renderViewMap = mutableMapOf<Activity, FragmentFlowRenderView>()
    private val subscriptions = mutableMapOf<Activity, Disposable>()

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        val store: ActivityStore<FragmentActivity> = findOrInitActivityStore(activity, savedInstance)

        // Give store a chance to initialize the activity.
        store.configureActivity?.invoke(activity)

        // Initialize render view
        val renderView = FragmentFlowRenderView(activity, onLifecycleEvent = store::onLifecycleEvent)
        renderViewMap[activity] = renderView
    }

    fun onActivityCreated(activity: FragmentActivity) {
        val store: ActivityStore<FragmentActivity>? = findStore(activity)
        if (store == null) {
            // TODO log missing store
            return
        }

        store.context.holder.attachActivity(activity)

        val renderView: FragmentFlowRenderView = renderViewOrThrow(activity)
        val disposable = store.state.subscribe {
            renderView.renderer.render(it)
            store.onRenderFragmentState?.invoke(activity, it)
        }
        subscriptions[activity] = disposable
    }

    fun onActivityStarted(activity: FragmentActivity) {
        val store: ActivityStore<FragmentActivity>? = findStore(activity)
        if (store == null) {
            // TODO log missing store
            return
        }

        store.context.holder.onActivityStarted(activity)
    }

    fun onSaveInstanceState(activity: FragmentActivity, outState: Bundle?) {
        val key = activityToKeyMap[activity]
        if (key != null) {
            outState?.putString(BUNDLE_KEY, key)
        }
    }

    fun onActivityDestroyed(activity: FragmentActivity) {
        subscriptions.remove(activity)?.dispose()
        renderViewMap.remove(activity)?.dispose()

        val store = findStore(activity)
        store?.context?.holder?.detachActivity(activity)

        val key = activityToKeyMap.remove(activity)
        if (key != null && activity.isFinishing) {
            clearActivityStore(key)
        }
    }

    fun onActivityResult(activity: FragmentActivity, result: ActivityResult) {
        findStore(activity)?.context?.onActivityResult(result)
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        return renderViewMap[activity]?.onBackPressed() ?: false
    }

    private fun findStore(activity: FragmentActivity): ActivityStore<FragmentActivity>? {
        val key = activityToKeyMap[activity]
        return key?.let {
            componentMap[it]
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : FragmentActivity> findOrInitActivityStore(
        activity: FragmentActivity, savedState: Bundle?
    ): ActivityStore<A> {
        val key = findOrGenerateActivityKey(activity, savedState) // generate new key
        activityToKeyMap[activity] = key

        val cached = componentMap[key] as? ActivityStore<A>?
        if (cached != null) {
            return cached
        }

        val component = factory.init(activity) as ActivityStore<A>?
        if (component != null) {
            // let's store the flow to the map
            componentMap[key] = component as ActivityStore<FragmentActivity>
            return component
        }

        throw IllegalStateException("no store was found for: ${activity::class.java.simpleName}")
    }

    private fun clearActivityStore(key: String) {
        val component = componentMap.remove(key)
        component?.subscription?.dispose()
    }

    /**
     * Key is persisted across configuration changes.
     */
    private fun findOrGenerateActivityKey(activity: Activity, savedState: Bundle?): String {
        return (activityToKeyMap[activity] // Try the map
            ?: savedState?.getString(BUNDLE_KEY) // Try the bundle
            ?: UUID.randomUUID().toString())
    }

    private fun renderViewOrThrow(activity: FragmentActivity): FragmentFlowRenderView {
        return renderViewMap[activity] ?: throw callOnPreCreateException(activity)
    }

    private fun callOnPreCreateException(activity: FragmentActivity): IllegalStateException {
        return IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): ${activity::class.java.simpleName}")
    }
}
