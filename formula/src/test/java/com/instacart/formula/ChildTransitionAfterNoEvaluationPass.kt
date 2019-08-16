package com.instacart.formula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula(SideEffectFormula(onSideEffect))
}
