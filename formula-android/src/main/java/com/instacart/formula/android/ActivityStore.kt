package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity

/**
 * An ActivityStore is responsible for managing state of multiple routes. It maps each
 * navigation destination [com.instacart.formula.android.RouteKey] to its state
 * management stream.
 *
 * @param navigationStore Navigation state management defined for this [Activity].
 * @param configureActivity This is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                          use this callback to inject the activity.
 * @param onCleared This is invoked when the activity store should be disposed of.
 */
class ActivityStore<Activity : FragmentActivity>(
    val navigationStore: NavigationStore = NavigationStore.EMPTY,
    val configureActivity: ((Activity) -> Unit)? = null,
    val onCleared: (() -> Unit)? = null,
)
