package com.instacart.formula

import com.instacart.formula.subjects.HasChildFormula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(SideEffectFormula(onSideEffect))
}
