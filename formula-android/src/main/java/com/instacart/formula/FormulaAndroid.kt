package com.instacart.formula

import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.ActivityConfigurator
import com.instacart.formula.android.internal.ActivityStoreFactory
import com.instacart.formula.android.internal.AppManager

object FormulaAndroid {

    private var application: Application? = null
    private var appManager: AppManager? = null

    /**
     * Initializes Formula Android integration. Should be called within [Application.onCreate].
     */
    fun init(
        application: Application,
        activities: ActivityConfigurator.() -> Unit
    ) {
        // Should we allow re-initialization?
        if (appManager != null) {
            throw IllegalStateException("can only initialize the store once.")
        }

        val factory = ActivityStoreFactory(activities)
        val appManager = AppManager(factory)
        application.registerActivityLifecycleCallbacks(appManager)

        this.application = application
        this.appManager = appManager
    }

    /**
     * Call this method in [FragmentActivity.onCreate] before calling [FragmentActivity.super.onCreate]
     */
    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        appManagerOrThrow().onPreCreate(activity, savedInstance)
    }

    /**
     * Used in testing to clear current store manager.
     */
    @VisibleForTesting fun reset() {
        val app = ensureInitialized(application)
        app.unregisterActivityLifecycleCallbacks(appManager)

        application = null
        appManager = null
    }


    internal fun appManagerOrThrow(): AppManager {
        return ensureInitialized(appManager)
    }

    private fun <T : Any> ensureInitialized(variable: T?): T {
        return checkNotNull(variable) { "Need to call FormulaAndroid.init() from your Application." }
    }
}
