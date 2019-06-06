package com.instacart.formula

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instacart.formula.integration.AppStoreFactory
import com.instacart.formula.integration.StoreManager
import com.instacart.formula.internal.FormulaActivityCallbacks
import java.lang.IllegalStateException

object FormulaAndroid {

    private var storeManager: StoreManager? = null
    private var callbacks: FormulaActivityCallbacks? = null


    fun init(application: Application, init: AppStoreFactory.Builder.() -> Unit) {
        // Should we allow re-initialization?

        val builder = AppStoreFactory.Builder()
        builder.init()
        val factory = builder.build()
        val manager = StoreManager(factory)
        val activityLifecycleCallbacks = FormulaActivityCallbacks(manager)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        this.storeManager
        this.callbacks = activityLifecycleCallbacks
    }

    fun onPreCreate(activity: FragmentActivity, savedInstance: Bundle?) {
        val manager = storeManager ?: throw IllegalStateException("call FormulaAndroid.init() from your Application: $activity")
        manager.onPreCreate(activity, savedInstance)
    }

    fun onActivityResult(activity: FragmentActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO
    }

    fun onBackPressed(activity: FragmentActivity): Boolean {
        val manager = storeManager ?: throw IllegalStateException("call FormulaAndroid.init() from your Application: $activity")
        return manager.onBackPressed(activity)
    }
}
