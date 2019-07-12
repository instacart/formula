package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.TransitionLockImpl
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerFormula
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class FormulaManagerChildrenTest {

    @Test
    fun `children should be cleaned up`() {

        val scheduler = TestScheduler()
        val formula = RootFormula(TimerFormula(Timer(scheduler)))
        val transitionLock = TransitionLockImpl()
        val manager = FormulaManagerImpl<Unit, RootFormula.State, Unit, RootFormula.RenderModel>(
            RootFormula.State(),
            transitionLock = transitionLock,
            childManagerFactory = FormulaManagerFactoryImpl()
        )

        manager.setTransitionListener { output, isValid ->
            transitionLock.next()
        }

        val result = manager.evaluate(formula, Unit, transitionLock.processingPass)
        result.renderModel.timer!!.onClose()

        val next = manager.evaluate(formula, Unit, transitionLock.processingPass)
        assertThat(next.renderModel.timer).isNull()

        assertThat(manager.frame!!.children[FormulaKey(TimerFormula::class, "")]).isNull()
    }
}
