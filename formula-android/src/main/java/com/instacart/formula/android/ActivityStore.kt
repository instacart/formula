package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.events.FragmentLifecycleEvent
import io.reactivex.rxjava3.disposables.Disposable

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentKey] to its state
 * management stream.
 *
 * @param fragmentStore Fragment state management defined for this [Activity].
 * @param streams This provides ability to configure arbitrary RxJava streams that survive
 *                configuration changes. Check [com.instacart.formula.android.StreamConfigurator] for utility methods.
 * @param configureActivity This is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                          use this callback to inject the activity.
 * @param onRenderFragmentState This is invoked after [FragmentState] has been updated.
 * @param onFragmentLifecycleEvent This is callback for when a fragment is added or removed.
 */
class ActivityStore<Activity : FragmentActivity>(
    val fragmentStore: FragmentStore = FragmentStore.EMPTY,
    val streams: (StreamConfigurator<Activity>.() -> Disposable)? = null,
    val configureActivity: ((Activity) -> Unit)? = null,
    val onRenderFragmentState: ((Activity, FragmentState) -> Unit)? = null,
    val onFragmentLifecycleEvent: ((FragmentLifecycleEvent) -> Unit)? = null
)
