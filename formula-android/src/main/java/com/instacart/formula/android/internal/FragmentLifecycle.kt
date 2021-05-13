package com.instacart.formula.android.internal

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentInspector
import androidx.fragment.app.FragmentManager
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.fragment.BaseFormulaFragment
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentLifecycleEvent

/**
 * Provides utility method [lifecycleEvents] to track what fragments are added and removed from the backstack.
 */
internal object FragmentLifecycle {

    internal fun shouldTrack(fragment: Fragment): Boolean {
        return !fragment.retainInstance && !FragmentInspector.isHeadless(fragment)
    }

    internal fun isKept(fragmentManager: FragmentManager, fragment: Fragment): Boolean {
        return !fragment.isRemoving
    }

    internal fun createAddedEvent(f: Fragment): FragmentLifecycleEvent.Added {
        return FragmentLifecycleEvent.Added(f.getFormulaFragmentId())
    }

    internal fun createRemovedEvent(f: Fragment): FragmentLifecycleEvent.Removed {
        val fragment = f as? BaseFormulaFragment<*>
        return FragmentLifecycleEvent.Removed(f.getFormulaFragmentId(), fragment?.currentState())
    }
}

private fun Fragment.getFragmentKey(): FragmentKey {
    val fragment = this as? BaseFormulaFragment<*>
    return fragment?.getFragmentKey() ?: EmptyFragmentKey(tag.orEmpty())
}

private fun Fragment.getFragmentInstanceId(): String {
    return arguments?.getString(FormulaFragment.ARG_FORMULA_ID) ?: ""
}

internal fun Fragment.getFormulaFragmentId(): FragmentId {
    return FragmentId(
        instanceId = getFragmentInstanceId(),
        key = getFragmentKey()
    )
}