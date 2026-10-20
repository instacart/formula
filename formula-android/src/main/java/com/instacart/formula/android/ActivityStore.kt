package com.instacart.formula.android

import androidx.fragment.app.FragmentActivity

/**
 * Stores activity-specific configuration and cleanup callbacks.
 *
 * @param configureActivity This is invoked as part of [com.instacart.formula.FormulaAndroid.onPreCreate]. You can
 *                          use this callback to inject the activity.
 * @param onCleared This is invoked when the activity store should be disposed of. Use it to clean
 *                  up activity-scoped resources, such as a [NavigationStore].
 */
class ActivityStore<Activity : FragmentActivity>(
    val configureActivity: ((Activity) -> Unit)? = null,
    val onCleared: (() -> Unit)? = null,
)
