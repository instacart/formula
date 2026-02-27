package com.instacart.formula.lifecycle

import com.instacart.formula.Formula

internal interface LifecycleComponent {

    /**
     * Called when component is first created in the cache during evaluation.
     * Actions use this to schedule their start effect.
     */
    fun onAttached(scheduler: LifecycleScheduler) = Unit

    /**
     * Called when component is detached from Formula. This happens when it is not
     * called during [Formula.evaluate].
     */
    fun onDetached(scheduler: LifecycleScheduler) = Unit

    /**
     * Called when [Formula] is removed. This should not trigger any external side-effects,
     * only mark itself and its children as terminated.
     */
    fun markAsTerminated() = Unit

    /**
     * Called after [markAsTerminated] to perform termination side-effects.
     */
    fun performTermination() = Unit

    /**
     * Called when a duplicate [key] is requested. Provides ability to log this error.
     */
    fun onDuplicateKey(log: DuplicateKeyLog, key: Any) = Unit
}
