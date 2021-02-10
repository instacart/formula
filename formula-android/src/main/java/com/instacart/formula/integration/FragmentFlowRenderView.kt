package com.instacart.formula.integration

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.instacart.formula.Renderer
import com.instacart.formula.RenderView
import com.instacart.formula.fragment.BaseFormulaFragment
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentEnvironment
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentLifecycle
import com.instacart.formula.fragment.FragmentLifecycleEvent
import com.instacart.formula.fragment.getFragmentContract
import com.instacart.formula.integration.internal.forEachIndices
import java.util.LinkedList

/**
 * Renders [FragmentFlowState] and provides back button handling.
 *
 * NOTE: Initialize this class before calling [FragmentActivity.super.onCreate]
 *
 * [activity] activity within which the [FragmentFlowRenderView] lives.
 * [onLifecycleEvent] fragment lifecycle events that should be passed to the [com.instacart.formula.fragment.FragmentFlowStore]
 */
internal class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val fragmentEnvironment: FragmentEnvironment,
    private val onLifecycleEvent: (FragmentLifecycleEvent) -> Unit,
    private val onLifecycleState: (FragmentContract<*>, Lifecycle.State) -> Unit,
    private val onFragmentViewStateChanged: (FragmentContract<*>, isVisible: Boolean) -> Unit
) : RenderView<FragmentFlowState> {

    private var fragmentState: FragmentFlowState? = null
    private val visibleFragments: LinkedList<Fragment> = LinkedList()

    private var removedEarly = mutableListOf<FragmentContract<*>>()
    private var backStackEntries = mutableListOf<FragmentManager.BackStackEntry>()
    private var stateRestored: Boolean = false

    private val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)

            if (!stateRestored) {
                recordBackstackChange()
                stateRestored = true
            }

            visibleFragments.add(f)

            if (f is FormulaFragment<*>) {
                f.setEnvironment(fragmentEnvironment)
            }

            fragmentState?.let {
                updateVisibleFragments(it)
            }

            onFragmentViewStateChanged(f.getFragmentContract(), true)
            notifyLifecycleStateChanged(f, Lifecycle.State.CREATED)
        }

        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            super.onFragmentStarted(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.STARTED)
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentResumed(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.RESUMED)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            super.onFragmentPaused(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.STARTED)
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
            super.onFragmentStopped(fm, f)
            notifyLifecycleStateChanged(f, Lifecycle.State.CREATED)
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)
            visibleFragments.remove(f)

            notifyLifecycleStateChanged(f, Lifecycle.State.DESTROYED)
            if (!removedEarly.contains(f.getFragmentContract())) {
                onFragmentViewStateChanged(f.getFragmentContract(), false)
            }
        }

        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
            super.onFragmentAttached(fm, f, context)
            if (FragmentLifecycle.shouldTrack(f)) {
                onLifecycleEvent(FragmentLifecycle.createAddedEvent(f))
            }
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            super.onFragmentDetached(fm, f)
            // Only trigger detach, when fragment is actually being removed from the backstack
            if (FragmentLifecycle.shouldTrack(f) && !FragmentLifecycle.isKept(fm, f)) {
                val event = FragmentLifecycle.createRemovedEvent(f)
                val wasRemovedEarly = removedEarly.remove(f.getFragmentContract())
                if (!wasRemovedEarly) {
                    onLifecycleEvent(event)
                }
            }
        }
    }

    init {
        activity.supportFragmentManager.addOnBackStackChangedListener {
            recordBackstackChange()
        }

        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(callback, false)
    }

    override val render: Renderer<FragmentFlowState> = Renderer {
        updateVisibleFragments(it)

        fragmentState = it
    }

    fun onBackPressed(): Boolean {
        val lastFragment = visibleFragments.lastOrNull()
        if (lastFragment is BaseFormulaFragment<*>) {
            val state = fragmentState?.states?.get(lastFragment.getFragmentContract())?.renderModel
            return state is BackCallback && state.onBackPressed()
        }
        return false
    }

    /**
     * This method must be invoked in [android.app.Activity.onDestroy]
     */
    fun dispose() {
        activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(callback)
    }

    private fun notifyLifecycleStateChanged(fragment: Fragment, newState: Lifecycle.State) {
        onLifecycleState.invoke(fragment.getFragmentContract(), newState)
    }

    private fun updateVisibleFragments(state: FragmentFlowState) {
        visibleFragments.forEachIndices { fragment ->
            if (fragment is BaseFormulaFragment<*>) {
                state.states[fragment.getFragmentContract()]?.let {
                    (fragment as BaseFormulaFragment<Any>).setState(it.renderModel)
                }
            }
        }
    }

    private fun recordBackstackChange() {
        val backStackEntryCount = backStackEntries.size
        val newBackStackEntryCount = activity.supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > newBackStackEntryCount) {
            val removedEntries = backStackEntries.drop(newBackStackEntryCount)
            removedEntries.forEach { removed ->
                val poppedFragmentName = removed.name
                if (poppedFragmentName != null) {
                    visibleFragments.find { it.tag == poppedFragmentName }?.let { poppedFragment ->
                        // In case backstack gets repopulated before onDestroyView/onFragmentDetached gets called,
                        // we internally clear it so it doesn't potentially interfere with a fragment that could have the same contract
                        removeFragment(poppedFragment)
                    }
                }
                backStackEntries.remove(removed)
            }
        } else if (backStackEntryCount < newBackStackEntryCount) {
            for (i in backStackEntryCount until newBackStackEntryCount) {
                backStackEntries.add(activity.supportFragmentManager.getBackStackEntryAt(i))
            }
        }
    }

    private fun removeFragment(fragment: Fragment) {
        onFragmentViewStateChanged(fragment.getFragmentContract(), false)
        val event = FragmentLifecycle.createRemovedEvent(fragment)
        onLifecycleEvent(event)
        removedEarly.add(fragment.getFragmentContract())
    }
}
