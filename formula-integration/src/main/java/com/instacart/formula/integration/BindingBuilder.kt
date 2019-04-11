package com.instacart.formula.integration

import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.CompositeBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

class BindingBuilder<Component, Key : Any> : BaseBindingBuilder<Component, Key>() {

    inline fun <reified T : Key, S : Any> register(noinline init: (T) -> Flowable<S>) = register(T::class, init)

    /**
     * Binds specific type of key to the render model management.
     */
    fun <T : Key, S : Any> register(type: KClass<T>, init: (T) -> Flowable<S>) = apply {
        val initWithScope = { _: Component, key: T ->
            init(key)
        }
        bind(type, initWithScope)
    }

    /**
     * Binds specific type of key to the render model management.
     */
    fun <T : Key, S : Any> bind(type: KClass<T>, init: (Component, T) -> Flowable<S>) = apply {
        val binding = Binding.single(type, init) as Binding<Component, Key>
        bind(binding)
    }

    fun <NewComponent> withScope(
        componentFactory: ComponentFactory<Component, NewComponent>,
        init: BindingBuilder<NewComponent, Key>.() -> Unit
    ) = apply {
        val scoped = BindingBuilder<NewComponent, Key>().apply(init).build()
        bind(CompositeBinding(componentFactory, scoped))
    }

    inline fun <reified T : Key> bind(integration: Integration<T, Key, *>) = apply {
        val init: (Component, T) -> Flowable<Any> = integration::create as (Component, T) -> Flowable<Any>
        bind(T::class, init)
    }
}
