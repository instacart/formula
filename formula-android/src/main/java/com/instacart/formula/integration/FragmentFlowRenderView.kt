package com.instacart.formula.integration

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
import io.reactivex.disposables.CompositeDisposable

/**
 * Renders [FragmentFlowState] and provides back button handling.
 *
 * NOTE: Initialize this class before calling [FragmentActivity.super.onCreate]
 *
 * [activity] - activity within which the [FragmentFlowRenderView] lives.
 * [onLifecycleEvent] - fragment lifecycle events that should be passed to the [com.instacart.formula.fragment.FragmentFlowStore]
 */
class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val onLifecycleEvent: (FragmentLifecycleEvent) -> Unit,
    private val onLifecycleState: ((FragmentContract<*>, Lifecycle.State) -> Unit)? = null
) : RenderView<FragmentFlowState> {

    private var fragmentState: FragmentFlowState? = null
    private var currentFragmentRenderModel: Any? = null

    private val disposables = CompositeDisposable()

    private val visibleFragments: MutableMap<String, Fragment> = mutableMapOf()

    private var backstackEntries: Int = 0
    private var backstackPopped: Boolean = false
    private var removedEarly = mutableListOf<FragmentContract<*>>()

    init {
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)

                recordBackstackChange()

                val tag = f.tag
                if (tag != null) {
                    visibleFragments[tag] = f
                }

                fragmentState?.let {
                    updateVisibleFragments(it)
                }

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
                visibleFragments.remove(f.tag)

                recordBackstackChange()

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
        }, false)

        disposables.add(FragmentLifecycle.lifecycleEvents(activity).subscribe {
            val shouldFireEvent = it !is LifecycleEvent.Removed || !removedEarly.remove(it.key)
            if (shouldFireEvent) {
                onLifecycleEvent(it)
            }
        })
    }

    override val renderer: Renderer<FragmentFlowState> = Renderer.create {
        updateVisibleFragments(it)

        fragmentState = it
        currentFragmentRenderModel = it.lastEntry()?.renderModel
    }

    fun onBackPressed(): Boolean {
        // TODO: only visible fragments should handle back presses.
        // currentFragmentRenderModel might not be currently visible fragment.
        val state = currentFragmentRenderModel
        return state is BackCallback && state.onBackPressed()
    }

    /**
     * This method must be invoked in [android.app.Activity.onDestroy]
     */
    fun dispose() {
        disposables.dispose()
    }

    private fun notifyLifecycleStateChanged(fragment: Fragment, newState: Lifecycle.State) {
        if (fragment is BaseFormulaFragment<*>) {
            onLifecycleState?.let {
                it.invoke(fragment.getFragmentContract(), newState)
            }
        }
    }

    private fun updateVisibleFragments(state: FragmentFlowState) {
        state.states.forEach { entry ->
            val fragment = visibleFragments[entry.key.tag]
            if (fragment != null && fragment is BaseFormulaFragment<*>) {
                (fragment as BaseFormulaFragment<Any>).setState(entry.value.renderModel)
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
