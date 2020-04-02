package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import io.reactivex.disposables.Disposable

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentContract] to its state
 * management stream.
 *
 * @param contracts Fragment state management defined for this [Activity].
 * @param streams This provides ability to configure arbitrary RxJava streams that survive
 *                configuration changes. Check [ActivityStoreContext.StreamConfigurator] for utility methods.
 * @param configureActivity This is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                          use this callback to inject the activity.
 * @param onRenderFragmentState This is invoked after [FragmentFlowState] has been updated.
 * @param onFragmentLifecycleEvent This is callback for when a fragment is added or removed.
 */
class ActivityStore<Activity : FragmentActivity>(
    val contracts: FragmentFlowStore,
    val streams: (() -> Disposable)? = null,
    val configureActivity: ((Activity) -> Unit)? = null,
    val onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
    val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null
)
