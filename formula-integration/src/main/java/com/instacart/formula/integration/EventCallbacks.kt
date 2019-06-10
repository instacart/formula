package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentLifecycleEvent

/**
 * A collection of various event callbacks.
 *
 * @param onInitActivity - this is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                         user this callback to inject the activity.
 * @param onRenderFragmentState - this is invoked after [FragmentFlowState] has been updated.
 * @param onFragmentLifecycleEvent - this is callback for when a fragment is added or removed.
 */
data class EventCallbacks<Activity : FragmentActivity>(
    val onInitActivity: ((Activity) -> Unit)? = null,
    val onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
    val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null
)
