package com.instacart.testutils.android

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.ActivityConfigurator
import com.instacart.formula.android.ActivityStore
import com.instacart.formula.android.ActivityStoreContext
import kotlin.reflect.KClass

class TestActivityConfigurator(
    private val activityConfigurator: ActivityConfigurator,
    private val onActivityContextInitialized: (ActivityContextInitEvent) -> Unit,
) {
    data class ActivityContextInitEvent(
        val type: KClass<*>,
        val activityContext: ActivityStoreContext<*>,
    )

    fun <A : FragmentActivity> activity(
        type: KClass<A>,
        init: ActivityStoreContext<A>.() -> ActivityStore<A>
    ) {
        activityConfigurator.activity(type) {
            onActivityContextInitialized(ActivityContextInitEvent(type, this))
            init()
        }
    }

    inline fun <reified A : FragmentActivity> activity(
        noinline init: ActivityStoreContext<A>.() -> ActivityStore<A>
    ) {
        activity(A::class, init)
    }
}

