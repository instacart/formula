package com.instacart.formula

import com.instacart.formula.subjects.HasChildFormula

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(HasChildFormula(SideEffectFormula(onSideEffect)))
}
