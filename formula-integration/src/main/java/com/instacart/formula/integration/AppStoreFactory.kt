package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

class AppStoreFactory internal constructor(
    private val bindings: Map<KClass<*>, Binding<*>>
) {
    internal class Binding<A : FragmentActivity>(
        val init: (ActivityProxy<A>) -> ActivityStore?
    )

    internal class StoreHolder<A : FragmentActivity>(
        val effectHandler: ActivityProxy<A>,
        val store: ActivityStore
    ) {
        val state = store.fragmentFlowStore.state().replay(1)
        val subscription = state.connect()
    }

    class Builder {
        private val bindings = mutableMapOf<KClass<*>, Binding<*>>()

        fun <A: FragmentActivity> bind(type: KClass<A>, init: (ActivityProxy<A>) -> ActivityStore?) {
            bindings[type] = Binding(init)
        }

        fun build(): AppStoreFactory {
            return AppStoreFactory(bindings)
        }
    }

    internal fun <A : FragmentActivity> init(activity: A): StoreHolder<A>? {
        val initializer = bindings[activity::class] as (ActivityProxy<A>) -> ActivityStore?
        val effectHandler = ActivityProxy<A>()
        val store = initializer(effectHandler)
        return store?.let {
            StoreHolder(effectHandler, store)
        }
    }
}
