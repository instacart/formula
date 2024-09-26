package com.instacart.formula.android.internal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentInspector
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.BaseFormulaFragment
import com.instacart.formula.android.FormulaFragment
import java.util.UUID

/**
 * Provides utility method [lifecycleEvents] to track what fragments are added and removed from the backstack.
 */
internal object FragmentLifecycle {

    internal fun shouldTrack(fragment: Fragment): Boolean {
        return !fragment.retainInstance && !FragmentInspector.isHeadless(fragment)
    }
}

private fun Fragment.getFragmentKey(): FragmentKey {
    val fragment = this as? BaseFormulaFragment<*>
    return fragment?.getFragmentKey() ?: EmptyFragmentKey(tag.orEmpty())
}

/**
 * Gets a persisted across configuration changes fragment identifier or initializes
 * one if it doesn't exist.
 */
private fun Fragment.getFragmentInstanceId(): String {
    return if (this is BaseFormulaFragment<*>) {
        val arguments = getOrSetArguments()
        val id = arguments.getString(FormulaFragment.ARG_FORMULA_ID, "")
        id.ifBlank {
            val initializedId = UUID.randomUUID().toString()
            arguments.putString(FormulaFragment.ARG_FORMULA_ID, initializedId)
            initializedId
        }
    } else {
        ""
    }
}

internal fun Fragment.getFormulaFragmentId(): FragmentId {
    return FragmentId(
        instanceId = getFragmentInstanceId(),
        key = getFragmentKey()
    )
}

internal fun Fragment.getOrSetArguments(): Bundle {
    return arguments ?: run {
        Bundle().apply {
            arguments = this
        }
    }
}