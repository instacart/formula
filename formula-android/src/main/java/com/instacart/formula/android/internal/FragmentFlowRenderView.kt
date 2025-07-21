package com.instacart.formula.android.internal

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.BackCallback
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.FragmentStore
import com.instacart.formula.android.getFormulaFragmentId
import java.util.LinkedList

/**
 * Renders [FragmentState] and provides back button handling.
 *
 * NOTE: Initialize this class before calling [FragmentActivity.super.onCreate]
 *
 * [activity] activity within which the [FragmentFlowRenderView] lives.
 */
internal class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val store: FragmentStore,
    private val onLifecycleState: (FragmentId<*>, Lifecycle.State) -> Unit,
    private val onFragmentViewStateChanged: (FragmentId<*>, isVisible: Boolean) -> Unit
) {
    private var fragmentState: FragmentState? = null
    private val visibleFragments: LinkedList<Fragment> = LinkedList()

    private val environment: FragmentEnvironment
        get() = store.environment

    private val callback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)

            visibleFragments.add(f)
            updateVisibleFragments()

            onFragmentViewStateChanged(f.getFormulaFragmentId(), true)
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
            onFragmentViewStateChanged(f.getFormulaFragmentId(), false)
        }

        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
            super.onFragmentAttached(fm, f, context)
            if (f is FormulaFragment) {
                f.fragmentStore = store
            }

            if (FragmentLifecycle.shouldTrack(f)) {
                val event = FragmentLifecycleEvent.Added(
                    fragmentId = f.getFormulaFragmentId(),
                )

                store.onLifecycleEvent(event)
            } else {
                environment.logger("Ignoring attach event for fragment: $f")
            }
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            super.onFragmentDetached(fm, f)

            // Only trigger detach, when fragment is actually being removed from the backstack
            if (FragmentLifecycle.shouldTrack(f) && f.isRemoving && !activity.isChangingConfigurations) {
                val formulaFragment = f as? FormulaFragment
                val event = FragmentLifecycleEvent.Removed(
                    fragmentId = f.getFormulaFragmentId(),
                    lastState = formulaFragment?.currentState(),
                )
                store.onLifecycleEvent(event)
            }
        }
    }

    init {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(callback, false)
    }

    fun render(state: FragmentState) {
        Utils.assertMainThread()

        store.onPreRenderFragmentState?.invoke(state)

        fragmentState = state
        updateVisibleFragments()
    }

    fun onBackPressed(): Boolean {
        val lastFragment = visibleFragments.lastOrNull()
        if (lastFragment is FormulaFragment) {
            val state = fragmentState?.outputs?.get(lastFragment.getFormulaFragmentId())?.renderModel
            return state is BackCallback && state.onBackPressed()
        }
        return false
    }

    private fun notifyLifecycleStateChanged(fragment: Fragment, newState: Lifecycle.State) {
        onLifecycleState.invoke(fragment.getFormulaFragmentId(), newState)
    }

    private fun updateVisibleFragments() {
        val state = fragmentState ?: return
        visibleFragments.forEachIndices { fragment ->
            if (fragment is FormulaFragment) {
                state.outputs[fragment.getFormulaFragmentId()]?.let {
                    fragment.setState(it.renderModel)
                }
            }
        }
    }
}
