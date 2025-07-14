package com.instacart.formula.android.internal

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentInspector
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

/**
 * Gets a persisted across configuration changes fragment identifier or initializes
 * one if it doesn't exist.
 */
internal fun Fragment.getFragmentInstanceId(): String {
    val arguments = getOrSetArguments()
    val id = arguments.getString(FormulaFragment.ARG_FORMULA_ID, "")
    return id.ifBlank {
        val initializedId = UUID.randomUUID().toString()
        arguments.putString(FormulaFragment.ARG_FORMULA_ID, initializedId)
        initializedId
    }
}

internal fun Fragment.getOrSetArguments(): Bundle {
    return arguments ?: run {
        Bundle().apply {
            arguments = this
        }
    }
}