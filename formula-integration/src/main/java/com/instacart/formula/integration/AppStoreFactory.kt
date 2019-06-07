package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowStore
import kotlin.reflect.KClass

class AppStoreFactory internal constructor(
    private val bindings: Map<KClass<*>, Binding<*>>
) {
    internal class Binding<A : FragmentActivity>(
        val init: (ActivityProxy<A>) -> FragmentFlowStore?
    )

    class Store<A : FragmentActivity>(
        val effectHandler: ActivityProxy<A>,
        val store: FragmentFlowStore
    ) {
        val state = store.state().replay(1)
        val subscription = state.connect()
    }

    class Builder {
        private val bindings = mutableMapOf<KClass<*>, Binding<*>>()

        fun <A: FragmentActivity> bind(type: KClass<A>, init: (ActivityProxy<A>) -> FragmentFlowStore?) {
            bindings[type] = Binding(init)
        }

        fun build(): AppStoreFactory {
            return AppStoreFactory(bindings)
        }
    }

    fun <A : FragmentActivity> init(activity: A): Store<A>? {
        val initializer = bindings[activity::class] as (ActivityProxy<A>) -> FragmentFlowStore?
        val effectHandler = ActivityProxy<A>()
        val store = initializer(effectHandler)
        return store?.let {
            Store(effectHandler, store)
        }
    }
}
