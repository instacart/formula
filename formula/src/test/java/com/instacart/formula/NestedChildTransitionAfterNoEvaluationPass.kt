package com.instacart.formula

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(HasChildFormula(SideEffectFormula(onSideEffect)))
}
