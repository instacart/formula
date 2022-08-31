package com.instacart.formula.android.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.events.ActivityResult
import java.util.UUID

/**
 * Manages activity state. It ensures that state survives configuration changes.
 */
internal class AppManager(
    private val factory: ActivityStoreFactory
) : Application.ActivityLifecycleCallbacks {

    companion object {
        private const val BUNDLE_KEY = "formula::activity::key"
    }

    private val activityToKeyMap = mutableMapOf<Activity, String>()
    private val componentMap = mutableMapOf<String, ActivityManager<FragmentActivity>>()

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        val store: ActivityManager<FragmentActivity> = findOrInitActivityStore(activity, savedInstance)
        store.onPreCreate(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            val store: ActivityManager<FragmentActivity>? = findStore(activity)
            if (store == null) {
                // TODO log missing store
                return
            }

            store.onActivityCreated(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is FragmentActivity) {
            val store: ActivityManager<FragmentActivity>? = findStore(activity)
            if (store == null) {
                // TODO log missing store
                return
            }

            store.onActivityStarted(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is FragmentActivity) {
            val store: ActivityManager<FragmentActivity>? = findStore(activity)
            if (store == null) {
                // TODO log missing store
                return
            }

            store.onActivityResumed(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity is FragmentActivity) {
            val key = activityToKeyMap[activity]
            if (key != null) {
                outState.putString(BUNDLE_KEY, key)
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity is FragmentActivity) {
            val store: ActivityManager<FragmentActivity>? = findStore(activity)
            if (store == null) {
                // TODO log missing store
                return
            }

            store.onActivityPaused(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is FragmentActivity) {
            val store: ActivityManager<FragmentActivity>? = findStore(activity)
            if (store == null) {
                // TODO log missing store
                return
            }

            store.onActivityStopped(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is FragmentActivity) {
            val store = findStore(activity)
            store?.onActivityDestroyed(activity)

            val key = activityToKeyMap.remove(activity)
            if (key != null && activity.isFinishing) {
                clearActivityStore(key)
            }
        }
    }

    fun onActivityResult(activity: FragmentActivity, result: ActivityResult) {
        findStore(activity)?.onActivityResult(result)
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        return findStore(activity)?.onBackPressed() ?: false
    }

    fun findStore(activity: FragmentActivity): ActivityManager<FragmentActivity>? {
        val key = activityToKeyMap[activity]
        return key?.let { componentMap[it] }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A : FragmentActivity> findOrInitActivityStore(
        activity: FragmentActivity, savedState: Bundle?
    ): ActivityManager<A> {
        val key = findOrGenerateActivityKey(activity, savedState) // generate new key
        activityToKeyMap[activity] = key

        val cached = componentMap[key] as? ActivityManager<A>?
        if (cached != null) {
            return cached
        }

        val component = factory.init(activity) as ActivityManager<A>?
        if (component != null) {
            // let's store the flow to the map
            componentMap[key] = component as ActivityManager<FragmentActivity>
            return component
        }

        throw IllegalStateException("no store was found for: ${activity::class.java.simpleName}")
    }

    private fun clearActivityStore(key: String) {
        val component = componentMap.remove(key)
        component?.dispose()
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
