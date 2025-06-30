package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity
import com.instacart.formula.android.events.FragmentLifecycleEvent

/**
 * An ActivityStore is responsible for managing state of multiple fragments. It maps each
 * navigation destination [com.instacart.formula.fragment.FragmentKey] to its state
 * management stream.
 *
 * @param fragmentStore Fragment state management defined for this [Activity].
 * @param configureActivity This is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                          use this callback to inject the activity.
 * @param onCleared This is invoked when the activity store should be disposed of.
 */
class ActivityStore<Activity : FragmentActivity>(
    val fragmentStore: FragmentStore = FragmentStore.EMPTY,
    val configureActivity: ((Activity) -> Unit)? = null,
    val onCleared: (() -> Unit)? = null,
)
