package com.instacart.formula.android

import android.os.Parcelable

/**
 * Identifies a route type and its navigation arguments. A host uses [tag] to distinguish route
 * types and to derive route-specific behavior.
 *
 * ```kotlin
 * @Parcelize
 * data class TaskDetailKey(val taskId: String) : RouteKey {
 *     override val tag: String = "task-detail"
 * }
 * ```
 */
interface RouteKey : Parcelable {
    /** Identifier used by the navigation host to distinguish route types. */
    val tag: String
}
