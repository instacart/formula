package com.instacart.formula.subjects

object TransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit): SideEffectFormula = SideEffectFormula(onSideEffect)
}
