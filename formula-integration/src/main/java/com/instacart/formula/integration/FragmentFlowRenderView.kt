package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import arrow.core.Option
import arrow.core.toOption
import com.instacart.formula.RenderView
import com.instacart.formula.Renderer
import com.instacart.formula.fragment.BaseFormulaFragment
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract
import com.instacart.formula.fragment.FragmentFlowState
import com.instacart.formula.fragment.FragmentLifecycle
import io.reactivex.disposables.CompositeDisposable

/**
 * Renders [FragmentFlowState] and provides back button handling.
 */
class FragmentFlowRenderView(
    private val activity: FragmentActivity,
    private val onLifecycleEvent: (LifecycleEvent<FragmentContract<*>>) -> Unit
) : RenderView<FragmentFlowState> {
    private var currentFragmentRenderModel: Any? = null

    private var lastFragment: FormulaFragment<*>? = null
    private var pendingUpdate: KeyState<FragmentContract<*>, *>? = null

    private val disposables = CompositeDisposable()

    init {
        activity.supportFragmentManager.addOnBackStackChangedListener {
            pendingUpdate?.let {
                applyState(it)
            }
        }

        disposables.add(FragmentLifecycle.lifecycleEvents(activity).subscribe(onLifecycleEvent))
    }

    private val updateRenderer = Renderer.create<Option<KeyState<FragmentContract<*>, *>>> {
        val state = it.orNull()

        if (state == null) {
            lastFragment = null
        } else {
            applyState(state)
        }

        currentFragmentRenderModel = state?.renderModel
    }

    override val renderer: Renderer<FragmentFlowState> = Renderer.create {
        updateRenderer.render(it.lastEntry().toOption())
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

    private fun applyState(state: KeyState<FragmentContract<*>, *>) {
        val tag = state.key.tag
        val fragment = lastFragment
            ?.takeIf { it.tag == tag && !it.isRemoving }
            ?: activity.supportFragmentManager.findFragmentByTag(tag)

        // Given the async fragment nature, sometimes state changes arrive
        // before fragment is ready to consume it. For example, there is
        // an issue with fragment animations where removed fragment is stuck
        // until next fragment transaction is executed. This introduce hard
        // to catch bug where if you navigate again to this fragment it will
        // send updates to the old one.
        if (fragment == null || fragment.isRemoving) {
            // No valid fragment
            lastFragment = null
            pendingUpdate = state
        } else {
            pendingUpdate = null

            if (fragment is BaseFormulaFragment<*>) {
                (fragment as BaseFormulaFragment<Any>).setState(state.renderModel!!)
            }

            lastFragment = fragment as? FormulaFragment<*>
        }
    }
}
