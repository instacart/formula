package com.instacart.formula

object TransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit): SideEffectFormula = SideEffectFormula(onSideEffect)
}
