package com.instacart.formula.integration

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowStore
import io.reactivex.disposables.Disposable
import java.lang.IllegalStateException
import java.util.UUID

/**
 * Manages [FragmentFlowStore] for individual activities. [FragmentFlowStore] survives configuration changes
 * by default.
 */
class StoreManager(
    private val factory: AppStoreFactory
) {

    companion object {
        private const val BUNDLE_KEY = "formula::activity::key"
    }

    private val activityToKeyMap = mutableMapOf<Activity, String>()
    private val componentMap = mutableMapOf<String, AppStoreFactory.Store<FragmentActivity>>()

    private val renderViewMap = mutableMapOf<Activity, FragmentFlowRenderView>()
    private val subscriptions = mutableMapOf<Activity, Disposable>()

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        // Find store holder
        val holder: AppStoreFactory.Store<FragmentActivity>? = findOrInitializeStore(activity, savedInstance)
        if (holder == null) {
            // TODO what should we do here?
            return
        }

        // Initialize render view
        val renderView = FragmentFlowRenderView(activity, onLifecycleEvent = holder.store::onLifecycleEffect)
        renderViewMap.put(activity, renderView)
    }


    fun onActivityCreated(activity: FragmentActivity) {
        val renderView: FragmentFlowRenderView = renderViewMap[activity] ?: throw IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): $activity")
        val store: AppStoreFactory.Store<FragmentActivity>? = findStore(activity)
        if (store == null) {
            // TODO log missing store
            return
        }
        store.effectHandler.activity = activity

        val disposable = store.state.subscribe {
            renderView.renderer.render(it)
        }
        subscriptions[activity] = disposable
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

        if (store?.effectHandler?.activity == activity) {
            store.effectHandler.activity = null
        }

        val key = activityToKeyMap.remove(activity)
        if (key != null && activity.isFinishing) {
//            Timber.d("finishing $activity, $key")
            clearComponent(key)
        }
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        return renderViewMap[activity]?.onBackPressed() ?: false
    }

    fun clearAll() {
        componentMap.clear()
    }

    private fun findStore(activity: FragmentActivity): AppStoreFactory.Store<FragmentActivity>? {
        val key = activityToKeyMap[activity]
        return key?.let {
            componentMap[it]
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : FragmentActivity> findOrInitializeStore(
        activity: FragmentActivity, savedState: Bundle?
    ): AppStoreFactory.Store<A>? {
        val key = findOrGenerateActivityKey(activity, savedState) // generate new key
        activityToKeyMap[activity] = key

        val cached = componentMap[key] as? AppStoreFactory.Store<A>?
        if (cached != null) {
            return cached
        }

        val component = factory.init(activity) as AppStoreFactory.Store<A>?
        if (component != null) {
//            Timber.d("initialized a new component for $activity, $key")
            // let's store the flow to the map
            componentMap[key] = component as AppStoreFactory.Store<FragmentActivity>
            return component
        }
//        Timber.d("couldn't find component for $activity, $key")
        return null
    }

    private fun clearComponent(key: String) {
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
}
