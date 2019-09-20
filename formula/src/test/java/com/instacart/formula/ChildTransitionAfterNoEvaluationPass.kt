package com.instacart.formula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) = HasChildFormula.create(SideEffectFormula.create(onSideEffect))
}
