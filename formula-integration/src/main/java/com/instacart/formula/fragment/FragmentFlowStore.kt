package com.instacart.formula.fragment

import com.instacart.formula.integration.BackstackStore
import com.instacart.formula.integration.FlowStore
import com.instacart.formula.integration.KeyBinding
import com.instacart.formula.integration.LifecycleEvent

/**
 * A simple store that allows you to handle multiple
 * fragment contracts and their states.
 */
class FragmentFlowStore(
    private val contractStore: BackstackStore<FragmentContract<*>>,
    private val store: FlowStore<FragmentContract<*>>
) {
    companion object {
        inline fun init(crossinline init: KeyBinding.Builder<Unit, Unit, FragmentContract<*>>.() -> Unit): FragmentFlowStore {
            val contractStore = BackstackStore<FragmentContract<*>>()
            val store = FlowStore.init(contractStore.stateChanges(), init)
            return FragmentFlowStore(contractStore, store)
        }
    }

    fun onLifecycleEffect(event: LifecycleEvent<FragmentContract<*>>) {
        contractStore.onLifecycleEffect(event)
    }

    fun screen() = store.screen()
}
