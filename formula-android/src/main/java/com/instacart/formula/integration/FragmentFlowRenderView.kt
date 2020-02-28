package com.instacart.formula.integration

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer
import com.instacart.formula.fragment.BaseFormulaFragment
import com.instacart.formula.fragment.FragmentContract
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
    private val onLifecycleEvent: (FragmentLifecycleEvent) -> Unit,
    private val onLifecycleState: (FragmentContract<*>, Lifecycle.State) -> Unit,
    private val onFragmentViewStateChanged: (FragmentContract<*>, isVisible: Boolean) -> Unit
) : RenderView<FragmentFlowState> {

    private var fragmentState: FragmentFlowState? = null
    private val visibleFragments: LinkedList<Fragment> = LinkedList()

    private var backstackEntries: Int = 0
    private var backstackPopped: Boolean = false
    private var removedEarly = mutableListOf<FragmentContract<*>>()

    private val callback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)

            backstackEntries = activity.supportFragmentManager.backStackEntryCount
            visibleFragments.add(f)

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

            onFragmentViewStateChanged(f.getFragmentContract(), false)
            notifyLifecycleStateChanged(f, Lifecycle.State.DESTROYED)
            // This means that fragment is removed due to backstack change.
            if (backstackPopped) {
                // Reset
                backstackPopped = false

                val event = FragmentLifecycle.createRemovedEvent(f)
                removedEarly.add(event.key)
                onLifecycleEvent(event)
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
                val wasRemovedEarly = removedEarly.remove(event.key)
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

    override val renderer: Renderer<FragmentFlowState> = Renderer.create {
        updateVisibleFragments(it)

        fragmentState = it
    }

    fun onBackPressed(): Boolean {
        val lastFragment = visibleFragments.last()
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
        val newBackstackSize = activity.supportFragmentManager.backStackEntryCount
        if (backstackEntries > newBackstackSize) {
            backstackPopped = true
        }
        backstackEntries = newBackstackSize
    }
}
