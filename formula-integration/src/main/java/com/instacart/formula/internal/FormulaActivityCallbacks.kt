package com.instacart.formula.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.integration.StoreManager

internal class FormulaActivityCallbacks(
    private val storeManager: StoreManager
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            activity.lifecycle
            storeManager.onActivityCreated(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity is FragmentActivity) {
            storeManager.onSaveInstanceState(activity, outState)
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is FragmentActivity) {
            storeManager.onActivityStarted(activity)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is FragmentActivity) {
            storeManager.onActivityDestroyed(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }
}
