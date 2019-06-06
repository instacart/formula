package com.instacart.formula.integration

import androidx.fragment.app.FragmentActivity

class ActivityEffectHandler<A : FragmentActivity> {
    internal var activity: A? = null

    fun send(effect: A.() -> Unit) {
        activity?.effect() ?: run {
            // Log missing activity.
        }
    }
}
