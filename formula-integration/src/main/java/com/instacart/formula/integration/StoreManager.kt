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
    private val componentMap = mutableMapOf<String, ActivityStoreHolder<FragmentActivity>>()

    private val renderViewMap = mutableMapOf<Activity, FragmentFlowRenderView>()
    private val subscriptions = mutableMapOf<Activity, Disposable>()

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        // Find store holder
        val storeHolder: ActivityStoreHolder<FragmentActivity>? = findOrInitActivityStore(activity, savedInstance)
        if (storeHolder == null) {
            /**
             * TODO:
             * This activity doesn't have a valid FragmentFlowStore. Should we use no-op store instead
             * of null for such scenarios instead and crash if it's missing completely?
             */
            return
        }

        // Initialize render view
        val renderView = FragmentFlowRenderView(activity, onLifecycleEvent = storeHolder.store::onLifecycleEvent)
        renderViewMap[activity] = renderView
    }


    fun onActivityCreated(activity: FragmentActivity) {
        val storeHolder: ActivityStoreHolder<FragmentActivity>? = findStore(activity)
        if (storeHolder == null) {
            // TODO log missing store
            return
        }

        storeHolder.effectHandler.attachActivity(activity)

        val renderView: FragmentFlowRenderView = renderViewOrThrow(activity)
        val disposable = storeHolder.state.subscribe {
            renderView.renderer.render(it)
            storeHolder.store.onRenderFragmentState?.invoke(activity, it)

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
        store?.effectHandler?.detachActivity(activity)

        val key = activityToKeyMap.remove(activity)
        if (key != null && activity.isFinishing) {
//            Timber.d("finishing $activity, $key")
            clearActivityStore(key)
        }
    }

    fun onActivityResult(activity: FragmentActivity, result: ActivityResult) {
        findStore(activity)?.effectHandler?.onActivityResult(result)
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        return renderViewMap[activity]?.onBackPressed() ?: false
    }

    private fun findStore(activity: FragmentActivity): ActivityStoreHolder<FragmentActivity>? {
        val key = activityToKeyMap[activity]
        return key?.let {
            componentMap[it]
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : FragmentActivity> findOrInitActivityStore(
        activity: FragmentActivity, savedState: Bundle?
    ): ActivityStoreHolder<A>? {
        val key = findOrGenerateActivityKey(activity, savedState) // generate new key
        activityToKeyMap[activity] = key

        val cached = componentMap[key] as? ActivityStoreHolder<A>?
        if (cached != null) {
            return cached
        }

        val component = factory.init(activity) as ActivityStoreHolder<A>?
        if (component != null) {
//            Timber.d("initialized a new component for $activity, $key")
            // let's store the flow to the map
            componentMap[key] = component as ActivityStoreHolder<FragmentActivity>
            return component
        }
//        Timber.d("couldn't find component for $activity, $key")
        return null
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
        return renderViewMap[activity]
            ?: throw IllegalStateException("please call onPreCreate before calling Activity.super.onCreate(): $activity")
    }
}
