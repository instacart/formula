package com.instacart.formula.android.internal

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.instacart.formula.android.BaseFormulaFragment
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentState
import com.instacart.formula.android.events.FragmentLifecycleEvent
import com.instacart.formula.android.BackCallback
import com.instacart.formula.android.FeatureEvent
import com.instacart.formula.android.FragmentId
import com.instacart.formula.android.ViewFactory
import com.instacart.formula.android.getFormulaFragmentId
import java.util.LinkedList

/**
 * Renders [FragmentState] and provides back button handling.
 *
 * NOTE: Initialize this class before calling [FragmentActivity.super.onCreate]
 *
 * [activity] activity within which the [FragmentFlowRenderView] lives.
 * [onLifecycleEvent] fragment lifecycle events that should be passed to the [com.instacart.formula.android.FragmentStore]
 */
internal class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val fragmentEnvironment: FragmentEnvironment,
    private val onLifecycleEvent: (FragmentLifecycleEvent) -> Unit,
    private val onLifecycleState: (FragmentId, Lifecycle.State) -> Unit,
    private val onFragmentViewStateChanged: (FragmentId, isVisible: Boolean) -> Unit
) {

    private var fragmentState: FragmentState? = null
    private var features: Map<FragmentId, FeatureEvent> = emptyMap()
    private val visibleFragments: LinkedList<Fragment> = LinkedList()

    private val featureProvider = object : FeatureProvider {
        override fun getFeature(id: FragmentId): FeatureEvent? {
            return features[id]
        }
    }

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
            if (FragmentLifecycle.shouldTrack(f)) {
                val event = FragmentLifecycleEvent.Added(
                    fragmentId = f.getFormulaFragmentId(),
                )
                onLifecycleEvent(event)
            } else {
                fragmentEnvironment.logger("Ignoring attach event for fragment: $f")
            }
        }

        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            super.onFragmentDetached(fm, f)

            // Only trigger detach, when fragment is actually being removed from the backstack
            if (FragmentLifecycle.shouldTrack(f) && f.isRemoving && !activity.isChangingConfigurations) {
                val formulaFragment = f as? BaseFormulaFragment<*>
                val event = FragmentLifecycleEvent.Removed(
                    fragmentId = f.getFormulaFragmentId(),
                    lastState = formulaFragment?.currentState(),
                )
                onLifecycleEvent(event)
            }
        }
    }

    init {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(callback, false)
    }

    fun render(state: FragmentState) {
        Utils.assertMainThread()

        fragmentState = state
        features = state.features
        updateVisibleFragments()
    }

    fun onBackPressed(): Boolean {
        val lastFragment = visibleFragments.lastOrNull()
        if (lastFragment is BaseFormulaFragment<*>) {
            val state = fragmentState?.outputs?.get(lastFragment.getFormulaFragmentId())?.renderModel
            return state is BackCallback && state.onBackPressed()
        }
        return false
    }

    fun viewFactory(fragment: FormulaFragment): ViewFactory<Any> {
        return FormulaFragmentViewFactory(
            environment = fragmentEnvironment,
            fragmentId = fragment.getFormulaFragmentId(),
            featureProvider = featureProvider,
        )
    }

    private fun notifyLifecycleStateChanged(fragment: Fragment, newState: Lifecycle.State) {
        onLifecycleState.invoke(fragment.getFormulaFragmentId(), newState)
    }

    private fun updateVisibleFragments() {
        val state = fragmentState ?: return
        visibleFragments.forEachIndices { fragment ->
            if (fragment is BaseFormulaFragment<*>) {
                state.outputs[fragment.getFormulaFragmentId()]?.let {
                    (fragment as BaseFormulaFragment<Any>).setState(it.renderModel)
                }
            }
        }
    }
}
