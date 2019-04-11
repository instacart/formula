package com.instacart.formula.integration

import com.instacart.formula.integration.internal.BaseBindingBuilder
import io.reactivex.Flowable
import kotlin.reflect.KClass

class BindingBuilder<in ParentComponent, out Component, Key : Any>(
    componentFactory: ComponentFactory<ParentComponent, Component>
) : BaseBindingBuilder<ParentComponent, Component, Key>(componentFactory){

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
        bind(
            Binding.single(
                type,
                init
            ) as Binding<Component, Key>
        )
    }

    fun <NewComponent> withScope(
        scopeFactory: ComponentFactory<Component, NewComponent>,
        init: BindingBuilder<*, NewComponent, Key>.() -> Unit
    ) = apply {
        val scoped = BindingBuilder<Component, NewComponent, Key>(scopeFactory).apply(init).build()
        bind(scoped)
    }

    inline fun <reified T : Key> bind(integration: Integration<T, *, *>) = apply {
        val init: (T) -> Flowable<Any> = integration::init as (T) -> Flowable<Any>
        register(T::class, init)
    }
}
