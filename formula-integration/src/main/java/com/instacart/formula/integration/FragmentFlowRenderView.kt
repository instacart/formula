package com.instacart.formula.integration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
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
 */
class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val onLifecycleEvent: (FragmentLifecycleEvent) -> Unit
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
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                super.onFragmentViewDestroyed(fm, f)
                visibleFragments.remove(f.tag)

                recordBackstackChange()

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
        val state = currentFragmentRenderModel
        if (state is BackCallback) {
            state.onBackPressed()
            return true
        }

        return false
    }

    /**
     * This method must be invoked in [android.app.Activity.onDestroy]
     */
    fun dispose() {
        disposables.dispose()
    }

    private fun updateVisibleFragments(state: FragmentFlowState) {
        state.states.forEach { entry ->
            val fragment = visibleFragments.get(entry.key.tag)
            if (fragment != null && fragment is BaseFormulaFragment<*>) {
                (fragment as BaseFormulaFragment<Any>).setState(entry.value.renderModel!!)
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
