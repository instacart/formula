package com.instacart.formula.integration

import com.instacart.formula.integration.internal.BaseBindingBuilder
import com.instacart.formula.integration.internal.CompositeBinding
import io.reactivex.rxjava3.core.Observable
import kotlin.reflect.KClass

class BindingBuilder<Component, Key : Any> : BaseBindingBuilder<Component, Key>() {

    inline fun <reified T : Key, S : Any> bind(noinline init: (T) -> Observable<S>) = bind(T::class, init)

    /**
     * Binds specific type of key to the render model management.
     */
    fun <T : Key, S : Any> bind(type: KClass<T>, init: (T) -> Observable<S>) = apply {
        val initWithScope = { _: Component, key: T ->
            init(key)
        }
        bind(type, initWithScope)
    }

    /**
     * Binds specific type of key to the render model management.
     */
    fun <T : Key, S : Any> bind(type: KClass<T>, init: (Component, T) -> Observable<S>) = apply {
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
        val init: (Component, T) -> Observable<Any> = integration::create as (Component, T) -> Observable<Any>
        bind(T::class, init)
    }
}
