package com.instacart.formula

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.internal.FormulaKey
import com.instacart.formula.internal.FormulaManagerImpl
import com.instacart.formula.internal.TransitionListener
import com.instacart.formula.internal.TransitionLockImpl
import org.junit.Test

class FormulaManagerChildrenTest {

    @Test
    fun `children should be cleaned up`() {

        val formula = OptionalChildFormula(StreamFormula())
        val transitionLock = TransitionLockImpl()
        val manager = FormulaManagerImpl(
            formula = formula,
            input = Unit,
            transitionLock = transitionLock,
            transitionListener = TransitionListener { _, _ ->
                transitionLock.next()
            }
        )

        val result = manager.evaluate(Unit, transitionLock.processingPass)
        val formulaKey = FormulaKey(StreamFormula::class, null)
        assertThat(manager.children!![formulaKey]).isNotNull()

        result.output.toggleChild()

        val next = manager.evaluate(Unit, transitionLock.processingPass)
        assertThat(next.output.child).isNull()

        assertThat(manager.children!![formulaKey]).isNull()
    }
}
