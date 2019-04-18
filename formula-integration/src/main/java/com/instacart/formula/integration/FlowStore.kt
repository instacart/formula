package com.instacart.formula.integration

import com.instacart.formula.integration.internal.CompositeBinding
import io.reactivex.Flowable

/**
 * A store that manages render model changes for each entry in the [BackStack].
 *
 * A simple example of how to initialize a store.
 * ```
 * val backstack = BackStackStore<Key>()
 * FlowStore.init(backstack.changes()) {
 *     bind(TaskListIntegration())
 *     bind(TaskDetailIntegration())
 *     bind(SettingsIntegration())
 * }
 * ```
 */
class FlowStore<Key : Any> constructor(
    keyState: Flowable<BackStack<Key>>,
    private val root: Binding<Unit, Key>
) {
    companion object {
        inline fun <Key : Any> init(
            state: Flowable<BackStack<Key>>,
            crossinline init: BindingBuilder<Unit, Key>.() -> Unit
        ): FlowStore<Key> {
            return init(Unit, state, init)
        }

        inline fun <Component, Key : Any> init(
            rootComponent: Component,
            state: Flowable<BackStack<Key>>,
            crossinline init: BindingBuilder<Component, Key>.() -> Unit
        ): FlowStore<Key> {
            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val root = BindingBuilder<Component, Key>()
                .apply(init)
                .build()

            val rootBinding = Binding.composite(factory, root)
            return FlowStore(state, rootBinding)
        }
    }

    private val reducerFactory = FlowReducers(root)
    private val keyState = keyState.replay(1).refCount()

    fun state(): Flowable<FlowState<Key>> {
        val backstackChangeReducer = keyState.map(reducerFactory::onBackstackChange)
        val stateChangeReducers = root.state(Unit, keyState).map(reducerFactory::onScreenStateChanged)

        val reducers = Flowable.merge(backstackChangeReducer, stateChangeReducers)

        return reducers
            .scan(FlowState<Key>()) { state, reducer ->
                reducer(state)
            }
            .distinctUntilChanged()
    }
}
