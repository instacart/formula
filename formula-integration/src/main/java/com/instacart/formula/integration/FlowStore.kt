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
 *     register(TaskListIntegration())
 *     register(TaskDetailIntegration())
 *     register(SettingsIntegration())
 * }
 * ```
 */
class FlowStore<Key : Any> constructor(
    keyState: Flowable<BackStack<Key>>,
    private val root: CompositeBinding<Key, Unit, Unit>
) {
    companion object {
        inline fun <Key : Any> init(
            state: Flowable<BackStack<Key>>,
            crossinline init: Binding.Builder<Unit, Unit, Key>.() -> Unit
        ): FlowStore<Key> {
            val root = Binding.Builder<Unit, Unit, Key>(componentFactory = {
                DisposableScope(component = Unit, onDispose = {})
            })
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
