package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.TransitionLockImpl
import com.instacart.formula.timer.Timer
import com.instacart.formula.timer.TimerEffect
import com.instacart.formula.timer.TimerFormula
import com.instacart.formula.timer.TimerRenderModel
import io.reactivex.schedulers.TestScheduler
import org.junit.Test

class FormulaManagerChildrenTest {

    @Test
    fun `children should be cleaned up`() {

        val scheduler = TestScheduler()
        val formula = OptionalChildFormula(TimerFormula(Timer(scheduler)))
        val transitionLock = TransitionLockImpl()
        val manager = FormulaManagerImpl<Unit, OptionalChildFormula.State, TimerEffect, OptionalChildFormula.RenderModel<TimerRenderModel>>(
            OptionalChildFormula.State(),
            transitionLock = transitionLock,
            childManagerFactory = FormulaManagerFactoryImpl()
        )

        manager.setTransitionListener { _, _ ->
            transitionLock.next()
        }

        val result = manager.evaluate(formula, Unit, transitionLock.processingPass)
        assertThat(manager.children[FormulaKey(TimerFormula::class, "")]).isNotNull()

        result.renderModel.toggleChild()

        val next = manager.evaluate(formula, Unit, transitionLock.processingPass)
        assertThat(next.renderModel.child).isNull()

        assertThat(manager.children[FormulaKey(TimerFormula::class, "")]).isNull()
    }
}
