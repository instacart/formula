package com.instacart.formula.android

import androidx.fragment.app.Fragment
import com.instacart.formula.android.internal.getFragmentInstanceId
import com.instacart.formula.android.internal.getFragmentKey

/**
 * An object used to identify a fragment. It combines both a user generated [key] and
 * a generated [String] id.
 *
 * @param instanceId Unique identifier used to distinguish a fragment. Since there
 * can be multiple fragments with the same [key], we use this as an extra identifier.
 *
 * @param key Fragment key used to create this fragment.
 */
data class FragmentId<out Type : FragmentKey>(
    val instanceId: String,
    val key: Type,
)

/**
 * Gets a [FragmentId] for a given [Fragment].
 */
fun Fragment.getFormulaFragmentId(): FragmentId<*> {
    return FragmentId(
        instanceId = getFragmentInstanceId(),
        key = getFragmentKey()
    )
}