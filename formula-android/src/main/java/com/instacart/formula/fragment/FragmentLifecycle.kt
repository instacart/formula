package com.instacart.formula.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentInspector
import androidx.fragment.app.FragmentManager

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
        return FragmentLifecycleEvent.Added(f.getFragmentKey())
    }

    internal fun createRemovedEvent(f: Fragment): FragmentLifecycleEvent.Removed {
        val fragment = f as? BaseFormulaFragment<*>
        return FragmentLifecycleEvent.Removed(f.getFragmentKey(), fragment?.currentState())
    }
}

internal fun Fragment.getFragmentKey(): FragmentKey {
    val fragment = this as? BaseFormulaFragment<*>
    return fragment?.getFragmentKey() ?: EmptyFragmentContract(tag.orEmpty())
}
