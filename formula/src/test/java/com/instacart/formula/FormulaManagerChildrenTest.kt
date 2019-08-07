package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.FormulaManagerFactoryImpl
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.JoinedKey
import com.instacart.formula.internal.ScopedCallbacks
import com.instacart.formula.internal.TransitionLockImpl
import org.junit.Test

class FormulaManagerChildrenTest {

    @Test
    fun `children should be cleaned up`() {

        val formula = OptionalChildFormula(StreamFormula())
        val transitionLock = TransitionLockImpl()
        val manager = FormulaManagerImpl<Unit, OptionalChildFormula.State, Unit, OptionalChildFormula.RenderModel<StreamFormula.RenderModel>>(
            OptionalChildFormula.State(),
            callbacks = ScopedCallbacks(formula),
            transitionLock = transitionLock,
            childManagerFactory = FormulaManagerFactoryImpl()
        )

        manager.setTransitionListener { _, _ ->
            transitionLock.next()
        }

        val result = manager.evaluate(formula, Unit, transitionLock.processingPass)
        assertThat(manager.children[JoinedKey("", StreamFormula::class)]).isNotNull()

        result.renderModel.toggleChild()

        val next = manager.evaluate(formula, Unit, transitionLock.processingPass)
        assertThat(next.renderModel.child).isNull()

        assertThat(manager.children[JoinedKey("", StreamFormula::class)]).isNull()
    }
}
