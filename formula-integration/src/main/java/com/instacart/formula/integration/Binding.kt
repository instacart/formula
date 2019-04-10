package com.instacart.formula.integration

import com.instacart.formula.integration.internal.CompositeBinding
import com.instacart.formula.integration.internal.SingleBinding
import io.reactivex.Flowable
import kotlin.reflect.KClass

/**
 * Defines how specific keys bind to the state management associated
 */
abstract class Binding<ParentComponent, Key, State> {

    class Builder<ParentComponent, Component, Key : Any>(
        private val componentFactory: ComponentFactory<ParentComponent, Component>
    ) {
        private val bindings: MutableList<Binding<Component, Key, *>> = mutableListOf()

        inline fun <reified T : Key, S> register(noinline init: (T) -> Flowable<S>) = register(T::class, init)

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> register(type: KClass<T>, init: (T) -> Flowable<S>) = apply {
            val initWithScope = { scope: Component, key: T ->
                init(key)
            }
            bind(type, init = initWithScope)
        }

        /**
         * Binds specific type of key to the render model management.
         */
        fun <T : Key, S> bind(type: KClass<T>, init: (Component, T) -> Flowable<S>) = apply {
            bind(SingleBinding(type.java, init) as SingleBinding<Key, Component, S>)
        }

        fun bind(binding: SingleBinding<Key, Component, *>) = apply {
            bindings.add(binding)
        }

        fun bind(binding: Binding<Component, Key, *>) = apply {
            bindings.add(binding)
        }

        fun <NewComponent> withScope(
            scopeFactory: (Component) -> DisposableScope<NewComponent>,
            init: Builder<Component, NewComponent, Key>.() -> Unit
        ) = apply {
            val scoped = Builder<Component, NewComponent, Key>(scopeFactory).apply(init).build()
            bind(scoped)
        }

        inline fun <reified T: Key> bind(integration: Integration<T, *, *>) = apply {
            val init: (T) -> Flowable<Any> = integration::init as (T) -> Flowable<Any>
            register(T::class, init)
        }

        fun build(): CompositeBinding<Key, ParentComponent, Component> {
            return CompositeBinding(componentFactory, bindings)
        }
    }

    /**
     * Determines if this binding handles the key
     */
    abstract fun binds(key: Any): Boolean

    abstract fun state(component: ParentComponent, store: Flowable<BackStack<Key>>): Flowable<KeyState<Key, State>>
}
