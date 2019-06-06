package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowStore
import kotlin.reflect.KClass

class AppStoreFactory internal constructor(
    private val bindings: Map<KClass<*>, Binding<*>>
) {
    internal class Binding<A : FragmentActivity>(
        val init: (ActivityEffectHandler<A>) -> FragmentFlowStore?
    )

    class Store<A : FragmentActivity>(
        val effectHandler: ActivityEffectHandler<A>,
        val store: FragmentFlowStore
    ) {
        val state = store.state().replay(1)
        val subscription = state.connect()
    }

    class Builder {
        private val bindings = mutableMapOf<KClass<*>, Binding<*>>()

        fun <A: FragmentActivity> bind(type: KClass<A>, init: (ActivityEffectHandler<A>) -> FragmentFlowStore?) {
            bindings[type] = Binding(init)
        }

        fun build(): AppStoreFactory {
            return AppStoreFactory(bindings)
        }
    }

    fun <A : FragmentActivity> init(activity: A): Store<A>? {
        val initializer = bindings[activity::class] as (ActivityEffectHandler<A>) -> FragmentFlowStore?
        val effectHandler = ActivityEffectHandler<A>()
        val store = initializer(effectHandler)
        return store?.let {
            Store(effectHandler, store)
        }
    }
}
