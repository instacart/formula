package com.instacart.formula.subjects

import com.instacart.formula.subjects.HasChildFormula
import com.instacart.formula.subjects.SideEffectFormula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(SideEffectFormula(onSideEffect))
}
