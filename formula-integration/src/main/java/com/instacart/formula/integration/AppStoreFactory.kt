package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

class AppStoreFactory internal constructor(
    private val bindings: Map<KClass<*>, Binding<*>>
) {
    internal class Binding<A : FragmentActivity>(
        val init: ActivityStoreContext<A>.() -> ActivityStore<A>?
    )

    class Builder {
        private val bindings = mutableMapOf<KClass<*>, Binding<*>>()

        fun <A : FragmentActivity> activity(
            type: KClass<A>,
            init: ActivityStoreContext<A>.() -> ActivityStore<A>?
        ) {
            bindings[type] = Binding(init)
        }

        fun build(): AppStoreFactory {
            return AppStoreFactory(bindings)
        }
    }

    internal fun <A : FragmentActivity> init(activity: A): ActivityStore<A>? {
        val initializer = bindings[activity::class] as? Binding<A>
        val activityProxy = ActivityProxy<A>()
        val activityStoreBuilder = ActivityStoreContext(activityProxy)
        return initializer?.init?.invoke(activityStoreBuilder)
    }
}
