package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity
import arrow.core.Option
import com.instacart.formula.fragment.BaseFormulaFragment
import com.instacart.formula.fragment.FormulaFragment
import com.instacart.formula.fragment.FragmentContract

/**
 * Handles application updates and provides access to back handling
 */
class FormulaActivityDelegate(private val activity: FragmentActivity) {
    private var currentFragmentRenderModel: Any? = null

    private var lastFragment: FormulaFragment<*>? = null
    private var pendingUpdate: KeyState<FragmentContract<*>, *>? = null

    init {
        activity.supportFragmentManager.addOnBackStackChangedListener {
            pendingUpdate?.let {
                applyUpdate(it)
            }
        }
    }

    fun applyUpdate(update: Option<KeyState<FragmentContract<*>, *>>) {
        val state = update.orNull()

        if (state == null) {
            lastFragment = null
        } else {
            applyUpdate(state)
        }

        currentFragmentRenderModel = update.orNull()?.state
    }

    private fun applyUpdate(state: KeyState<FragmentContract<*>, *>) {
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
                (fragment as BaseFormulaFragment<Any>).setState(state.state!!)
            }

            lastFragment = fragment as? FormulaFragment<*>
        }
    }

    fun onBackPressed(): Boolean {
        val state = currentFragmentRenderModel
        if (state is BackCallback) {
            state.onBackPressed()
            return true
        }

        return false
    }
}
