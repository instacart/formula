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

    /**
     * Initializes Formula Android integration. Should be called within [Application.onCreate].
     *
     * @param logger A logger for debug Formula Android events.
     * @param onFragmentError A global handler for fragment errors. Override this to log the crashes.
     */
    fun init(
        application: Application,
        logger: (String) -> Unit = {},
        onFragmentError: (FragmentKey, Throwable) -> Unit = { _, it -> throw it },
        activities: ActivityConfigurator.() -> Unit
    ) {
        // Should we allow re-initialization?
        if (appManager != null) {
            throw IllegalStateException("can only initialize the store once.")
        }

        val fragmentEnvironment = FragmentEnvironment(logger, onFragmentError)
        val factory = ActivityStoreFactory(fragmentEnvironment, activities)
        val appManager = AppManager(factory)
        application.registerActivityLifecycleCallbacks(appManager)

        this.application = application
        this.appManager = appManager
    }

    /**
     * Call this method in [FragmentActivity.onCreate] before calling [FragmentActivity.super.onCreate]
     */
    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        managerOrThrow(activity).onPreCreate(activity, savedInstance)
    }

    /**
     * Call this method in [FragmentActivity.onActivityResult]
     */
    fun onActivityResult(activity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        val result = ActivityResult(requestCode, resultCode, data)
        managerOrThrow(activity).onActivityResult(activity, result)
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
        return managerOrThrow(activity).onBackPressed(activity)
    }

    private fun managerOrThrow(activity: FragmentActivity): AppManager {
        return appManager ?: throw IllegalStateException("call FormulaAndroid.init() from your Application: $activity")
    }

    /**
     * Used in testing to clear current store manager.
     */
    @VisibleForTesting fun reset() {
        val app = application ?: throw IllegalStateException("not initialized")
        app.unregisterActivityLifecycleCallbacks(appManager)

        application = null
        appManager = null
    }
}
