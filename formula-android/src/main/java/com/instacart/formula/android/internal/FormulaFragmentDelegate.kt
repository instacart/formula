package com.instacart.formula.android.internal

import com.instacart.formula.android.FormulaFragment
import com.instacart.formula.android.FragmentEnvironment
import com.instacart.formula.android.FragmentKey
import com.instacart.formula.android.ViewFactory

internal object FormulaFragmentDelegate {
    var appManager: AppManager? = null
    var fragmentEnvironment: FragmentEnvironment? = null

    fun viewFactory(fragment: FormulaFragment): ViewFactory<Any>? {
        val appManager = appManager ?: throw IllegalStateException("FormulaAndroid.init() not called.")

        val activity = fragment.activity ?: run {
            fragmentEnvironment().logger("FormulaFragment has no activity attached: ${fragment.getFragmentKey()}")
            return null
        }

        val viewFactory = appManager.findStore(activity)?.viewFactory(fragment) ?: run {
            // Log view factory is missing
            if (activity.isDestroyed) {
                fragmentEnvironment().logger("Missing formula fragment view factory because activity is destroyed")
            } else {
                val error = IllegalStateException("Formula with ${fragment.getFragmentKey()} is missing view factory.")
                fragmentEnvironment().onScreenError(fragment.getFragmentKey(), error)
            }

            return null
        }
        return viewFactory
    }


    fun logFragmentError(key: FragmentKey, error: Throwable) {
        fragmentEnvironment().onScreenError(key, error)
    }

    private fun fragmentEnvironment(): FragmentEnvironment {
        return fragmentEnvironment ?: throw IllegalStateException("FormulaAndroid.init() not called.")
    }
}