package com.instacart.formula.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentInspector
import androidx.fragment.app.FragmentManager
import com.instacart.formula.integration.LifecycleEvent

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

    internal fun createAddedEvent(f: Fragment): LifecycleEvent.Added<FragmentContract<Nothing>> {
        return LifecycleEvent.Added(f.contract())
    }

    internal fun createRemovedEvent(f: Fragment): LifecycleEvent.Removed<FragmentContract<Nothing>> {
        val fragment = f as? BaseFormulaFragment<*>
        return LifecycleEvent.Removed(f.contract(), fragment?.currentState())
    }

    internal fun Fragment.contract(): FragmentContract<*> {
        val fragment = this as? BaseFormulaFragment<*>
        return fragment?.getFragmentContract() ?: EmptyFragmentContract(tag.orEmpty())
    }
}
