package com.instacart.formula.android

import android.os.Parcelable

/**
 * A key identifies a route and connects it to the corresponding [FeatureFactory].
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

    val tag: String
}
