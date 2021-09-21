package com.instacart.formula.subjects

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(HasChildFormula(SideEffectFormula(onSideEffect)))
}
