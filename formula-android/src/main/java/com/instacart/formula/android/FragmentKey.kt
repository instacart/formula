package com.instacart.formula.android

import android.os.Parcelable
import androidx.fragment.app.FragmentManager
import java.util.UUID

/**
 * A key is used to identify a specific [FormulaFragment] and to connect the correct [FeatureFactory].
 *
 * ```
 * data class TaskDetailKey(
 *   val taskID: String
 * ) : FragmentKey() {
 *   override val tag: String = "task-detail-$taskId"
 * }
 * ```
 */
abstract class FragmentKey : Parcelable {

    /**
     * Tag is a unique identifier used to distinguish each fragment instance by the [FragmentManager].
     */
    abstract val tag: String

    /**
     * A unique identifier used to distinguish a fragment. Since there can be multiple
     * fragments with same parameters, we use this as an extra identifier.
     */
    private var instanceId: String? = null

    fun setInstanceId(value: String) {
        instanceId = value
    }

    fun getOrInitInstanceId(): String {
        return instanceId ?: run {
            UUID.randomUUID().toString().apply {
                instanceId = this
            }
        }
    }

    fun getInstanceId(): String? {
        return instanceId
    }
}
