package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentFlowStore
import com.instacart.formula.fragment.FragmentLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentContract] to its state
 * management stream.
 *
 * @param configureActivity - this is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                         user this callback to inject the activity.
 * @param onRenderFragmentState - this is invoked after [FragmentFlowState] has been updated.
 * @param onFragmentLifecycleEvent - this is callback for when a fragment is added or removed.
 */
class ActivityStore<Activity : FragmentActivity>(
    val context: ActivityStoreContext<Activity>,
    val fragmentFlowStore: FragmentFlowStore,
    val start: (() -> Disposable)? = null,
    val configureActivity: ((Activity) -> Unit)? = null,
    val onRenderFragmentState: ((Activity, FragmentFlowState) -> Unit)? = null,
    private val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null,
    onFragmentFlowStateChanged: (FragmentFlowState) -> Unit
) {

    val state = fragmentFlowStore
        .state()
        .doOnNext(onFragmentFlowStateChanged)
        .replay(1)

    val subscription: Disposable

    init {
        if (start != null) {
            val disposables = CompositeDisposable()
            disposables.add(state.connect())
            disposables.add(start.invoke())
            subscription = disposables
        } else {
            subscription = state.connect()
        }
    }

    fun onLifecycleEvent(event: FragmentLifecycleEvent) {
        fragmentFlowStore.onLifecycleEffect(event)
        onFragmentLifecycleEvent?.invoke(event)
    }
}
