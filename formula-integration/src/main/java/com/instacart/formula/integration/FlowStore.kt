package com.instacart.formula.integration

import io.reactivex.Flowable

/**
 * A store that manages render model changes for each entry in the [BackStack].
 *
 * A simple example of how to initialize a store.
 * ```
 * val backstack = BackStackStore<Key>()
 * FlowStore.init(backstack.changes()) {
 *     register(TaskListIntegration())
 *     register(TaskDetailIntegration())
 *     register(SettingsIntegration())
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
            crossinline init: BindingBuilder<Unit, Unit, Key>.() -> Unit
        ): FlowStore<Key> {
            return init(Unit, state, init)
        }

        inline fun <Component, Key : Any> init(
            rootComponent: Component,
            state: Flowable<BackStack<Key>>,
            crossinline init: BindingBuilder<Unit, Component, Key>.() -> Unit
        ): FlowStore<Key> {
            val factory: (Unit) -> DisposableScope<Component> = {
                DisposableScope(component = rootComponent, onDispose = {})
            }

            val root = BindingBuilder<Unit, Component, Key>(componentFactory = factory)
                .apply(init)
                .build()

            return FlowStore(state, root)
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
