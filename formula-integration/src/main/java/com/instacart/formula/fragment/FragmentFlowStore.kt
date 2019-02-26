package com.instacart.formula.fragment

import com.instacart.formula.integration.BackStackStore
import com.instacart.formula.integration.FlowStore
import com.instacart.formula.integration.KeyBinding
import com.instacart.formula.integration.LifecycleEvent
import io.reactivex.Flowable

/**
 * A FragmentFlowStore is responsible for managing the state of multiple [FragmentContract] instances.
 */
class FragmentFlowStore(
    private val contractStore: BackStackStore<FragmentContract<*>>,
    private val store: FlowStore<FragmentContract<*>>
) {
    companion object {
        inline fun init(crossinline init: KeyBinding.Builder<Unit, Unit, FragmentContract<*>>.() -> Unit): FragmentFlowStore {
            val contractStore = BackStackStore<FragmentContract<*>>()
            val store = FlowStore.init(contractStore.stateChanges(), init)
            return FragmentFlowStore(contractStore, store)
        }
    }

    fun onLifecycleEffect(event: LifecycleEvent<FragmentContract<*>>) {
        contractStore.onLifecycleEffect(event)
    }

    fun state(): Flowable<FragmentFlowState> {
        return store.state()
    }
}
