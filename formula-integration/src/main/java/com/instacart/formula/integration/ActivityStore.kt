package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentContract] to its state
 * management stream.
 *
 * @param onInitActivity - this is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                         user this callback to inject the activity.
 * @param onRenderFragmentState - this is invoked after [FragmentFlowState] has been updated.
 */
class ActivityStore<A : FragmentActivity>(
    internal val onInitActivity: ((A) -> Unit)? = null,
    internal val onRenderFragmentState: ((A, FragmentFlowState) -> Unit)? = null,
    internal val proxy: ActivityProxy<A>,
    internal val fragmentFlowStore: FragmentFlowStore
) {

    internal val state = fragmentFlowStore.state().replay(1)
    internal val subscription = state.connect()

    internal fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        fragmentFlowStore.onLifecycleEffect(event)
    }
}
