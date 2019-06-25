package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.ProcessorManager
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerProcessorFormula
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class ProcessorManagerChildrenTest {


    @Test fun `children should be cleaned up`() {

        val scheduler = TestScheduler()
        val formula = RootFormula(TimerProcessorFormula(Timer(scheduler)))
        val manager = ProcessorManager<Unit, RootFormula.State, Unit>(
            RootFormula.State(),
            onTransition = {
                // TODO
            })

        val result = manager.process(formula, Unit)
        result.renderModel.timer!!.onClose()

        val next = manager.process(formula, Unit)
        assertThat(next.renderModel.timer).isNull()

        assertThat(manager.frame!!.children[FormulaKey(TimerProcessorFormula::class, "")]).isNull()
    }
}
