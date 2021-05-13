package com.instacart.formula.android

import android.os.Parcelable
import androidx.fragment.app.FragmentManager

/**
 * A key is used to identify a specific [FormulaFragment] and to connect the correct [FeatureFactory].
 *
 * ```
 * data class TaskDetailKey(
 *   val taskID: String
 * ) : FragmentKey {
 *   override val tag: String = "task-detail-$taskId"
 * }
 * ```
 */
interface FragmentKey : Parcelable {

    /**
     * Tag is a unique identifier used to distinguish each fragment instance by the [FragmentManager].
     */
    val tag: String
}
