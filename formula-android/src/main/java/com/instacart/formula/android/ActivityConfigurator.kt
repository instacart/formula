package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.integration.ActivityStore
import com.instacart.formula.integration.ActivityStoreContext
import kotlin.reflect.KClass

/**
 * Allows you provide a configuration for each type of Activity.
 */
class ActivityConfigurator {
    internal class Binding<A : FragmentActivity>(
        val init: ActivityStoreContext<A>.() -> ActivityStore<A>?
    )

    internal val bindings = mutableMapOf<KClass<*>, Binding<*>>()

    fun <A : FragmentActivity> activity(
        type: KClass<A>,
        init: ActivityStoreContext<A>.() -> ActivityStore<A>
    ) {
        bindings[type] = Binding(init)
    }

    inline fun <reified A : FragmentActivity> activity(
        noinline init: ActivityStoreContext<A>.() -> ActivityStore<A>
    ) {
        activity(A::class, init)
    }
}