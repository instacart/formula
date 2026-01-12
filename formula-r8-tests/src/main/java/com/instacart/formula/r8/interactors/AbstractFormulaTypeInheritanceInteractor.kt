package com.instacart.formula.r8.interactors

import com.google.common.truth.Truth
import com.instacart.formula.Evaluation
import com.instacart.formula.Snapshot
import com.instacart.formula.StatelessFormula
import com.instacart.formula.r8.fixtures.AbstractClassInheritanceFormulas
import com.instacart.formula.test.test
import kotlinx.coroutines.test.TestScope

/**
 * This interactor indirectly checks that
 * ```
 * val firstType = AbstractClassInheritanceFormulas.One()
 * val secondType = AbstractClassInheritanceFormulas.Two()
 * Truth.assertThat(firstType.type()).isNotEqualTo(secondType.type())
 * ```
 */
class AbstractFormulaTypeInheritanceInteractor(testScope: TestScope) {
    private val formula = object : StatelessFormula<Unit, Int>() {

        override fun Snapshot<Unit, Unit>.evaluate(): Evaluation<Int> {
            val one = context.child(AbstractClassInheritanceFormulas.One())
            val two = context.child(AbstractClassInheritanceFormulas.Two())

            return Evaluation(one + two)
        }
    }

    private val observer = formula.test(testScope)

    fun start() = apply {
        observer.input(Unit)
    }

    fun assertValue(value: Int) = apply {
        observer.output {
            Truth.assertThat(this).isEqualTo(value)
        }
    }
}