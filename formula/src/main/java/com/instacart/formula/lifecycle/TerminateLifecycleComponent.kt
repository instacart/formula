package com.instacart.formula.lifecycle

import com.instacart.formula.plugin.FormulaError
import com.instacart.formula.runtime.FormulaManagerImpl

internal class TerminateLifecycleComponent(
    private val manager: FormulaManagerImpl<*, *, *>,
) : LifecycleComponent {
    var terminationEffect: (() -> Unit)? = null

    override fun onDetached(scheduler: LifecycleScheduler) {
        scheduler.scheduleTerminateEffect(this::performTermination)
    }

    override fun performTermination() {
        try {
            terminationEffect?.invoke()
        } catch (e: Throwable) {
            val error = FormulaError.Unhandled(
                formula = manager.formulaType,
                error = e,
            )
            manager.onError(error)
        } finally {
            terminationEffect = null
        }
    }
}