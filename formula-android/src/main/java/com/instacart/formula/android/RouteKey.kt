package com.instacart.formula.android

import android.os.Parcelable
import androidx.fragment.app.FragmentManager

/**
 * A key is used to identify a specific [FormulaFragment] and to connect the correct [FeatureFactory].
 *
 * ```
 * data class TaskDetailKey(
 *   val taskID: String
 * ) : RouteKey {
 *   override val tag: String = "task-detail-$taskId"
 * }
 * ```
 */
interface RouteKey : Parcelable {

    /**
     * Tag is a unique identifier used to distinguish each route instance by the [FragmentManager].
     */
    val tag: String
}

@Deprecated(
    message = "FragmentKey has been renamed to RouteKey",
    replaceWith = ReplaceWith("RouteKey", "com.instacart.formula.android.RouteKey")
)
typealias FragmentKey = RouteKey
