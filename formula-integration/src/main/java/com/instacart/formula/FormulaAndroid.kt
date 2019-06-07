package com.instacart.formula

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.activity.ActivityResult
import com.instacart.formula.integration.AppStoreFactory
import com.instacart.formula.integration.StoreManager
import com.instacart.formula.internal.FormulaActivityCallbacks
import java.lang.IllegalStateException

object FormulaAndroid {

    private var storeManager: StoreManager? = null
    private var callbacks: FormulaActivityCallbacks? = null

    fun init(application: Application, init: AppStoreFactory.Builder.() -> Unit) {
        // Should we allow re-initialization?
        if (storeManager != null) {
            throw IllegalStateException("can only initialize the store once.")
        }

        val factory = AppStoreFactory.Builder().also { it.init() }.build()
        val manager = StoreManager(factory)
        val activityLifecycleCallbacks = FormulaActivityCallbacks(manager)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        this.storeManager = manager
        this.callbacks = activityLifecycleCallbacks
    }

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        managerOrThrow(activity).onPreCreate(activity, savedInstance)
    }

    fun onActivityResult(activity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        val result = ActivityResult(requestCode, resultCode, data)
        managerOrThrow(activity).onActivityResult(activity, result)
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        return managerOrThrow(activity).onBackPressed(activity)
    }

    private fun managerOrThrow(activity: FragmentActivity): StoreManager {
        return storeManager ?: throw IllegalStateException("call FormulaAndroid.init() from your Application: $activity")
    }
}
