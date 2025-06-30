package com.instacart.formula.android.internal

import com.instacart.formula.FormulaAndroid
import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.ViewFactory

internal object FormulaFragmentDelegate {
    fun viewFactory(
        fragmentEnvironment: FragmentEnvironment,
        fragment: FormulaFragment,
    ): ViewFactory<Any>? {
        val appManager = FormulaAndroid.appManagerOrThrow()
        val activity = fragment.requireActivity()
        val viewFactory = appManager.findStore(activity)?.viewFactory(fragment) ?: run {
            // Log view factory is missing
            if (activity.isDestroyed) {
                fragmentEnvironment.logger("Missing view factory because activity is destroyed: ${fragment.getFragmentKey()}")
            } else {
                val error = IllegalStateException("Formula with ${fragment.getFragmentKey()} is missing view factory.")
                fragmentEnvironment.onScreenError(fragment.getFragmentKey(), error)
            }

            return null
        }
        return viewFactory
    }
}