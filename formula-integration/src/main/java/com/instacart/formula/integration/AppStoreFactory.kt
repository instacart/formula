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
            init: ActivityStoreContext<A>.() -> ActivityStore<A>
        ) {
            bindings[type] = Binding(init)
        }

        inline fun <reified A : FragmentActivity> activity(
            noinline init: ActivityStoreContext<A>.() -> ActivityStore<A>
        ) {
            activity(A::class, init)
        }

        fun build(): AppStoreFactory {
            return AppStoreFactory(bindings)
        }
    }

    internal fun <A : FragmentActivity> init(activity: A): ActivityStore<A>? {
        val initializer = bindings[activity::class] as? Binding<A> ?: return null

        val holder = ActivityHolder<A>()
        val activityStoreBuilder = ActivityStoreContext(holder)
        return initializer.init.invoke(activityStoreBuilder)
    }
}
