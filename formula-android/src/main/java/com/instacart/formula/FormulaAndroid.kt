package com.instacart.formula

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.ActivityConfigurator
import com.instacart.formula.android.events.ActivityResult
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.internal.ActivityStoreFactory
import com.instacart.formula.android.internal.AppManager
import com.instacart.formula.android.FragmentKey
import java.lang.IllegalStateException

object FormulaAndroid {

    private var application: Application? = null
    private var appManager: AppManager? = null
    private var fragmentEnvironment: FragmentEnvironment? = null

    /**
     * Initializes Formula Android integration. Should be called within [Application.onCreate].
     *
     * @param fragmentEnvironment Environment model that configures various event listeners.
     */
    fun init(
        application: Application,
        fragmentEnvironment: FragmentEnvironment = FragmentEnvironment(),
        activities: ActivityConfigurator.() -> Unit
    ) {
        // Should we allow re-initialization?
        if (appManager != null) {
            throw IllegalStateException("can only initialize the store once.")
        }

        val factory = ActivityStoreFactory(fragmentEnvironment, activities)
        val appManager = AppManager(factory)
        application.registerActivityLifecycleCallbacks(appManager)

        this.application = application
        this.appManager = appManager
        this.fragmentEnvironment = fragmentEnvironment
    }

    /**
     * Call this method in [FragmentActivity.onCreate] before calling [FragmentActivity.super.onCreate]
     */
    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        appManagerOrThrow().onPreCreate(activity, savedInstance)
    }

    /**
     * Call this method in [FragmentActivity.onActivityResult]
     */
    fun onActivityResult(activity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        val result = ActivityResult(requestCode, resultCode, data)
        appManagerOrThrow().onActivityResult(activity, result)
    }

    /**
     * This method checks if the current fragments render model implements [com.instacart.formula.integration.BackCallback]
     * and will invoke that, otherwise returns false.
     *
     * It is commonly used as follows
     * ```
     * class MyActivity : FragmentActivity() {
     *     override fun onBackPressed() {
     *          if (!FormulaAndroid.onBackPressed(this)) {
     *              super.onBackPressed()
     *          }
     *     }
     * }
     * ```
     */
    fun onBackPressed(activity: FragmentActivity): Boolean {
        return appManagerOrThrow().onBackPressed(activity)
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

    internal fun fragmentEnvironment(): FragmentEnvironment {
        return ensureInitialized(fragmentEnvironment)
    }

    private fun <T : Any> ensureInitialized(variable: T?): T {
        return checkNotNull(variable) { "Need to call FormulaAndroid.init() from your Application." }
    }
}
