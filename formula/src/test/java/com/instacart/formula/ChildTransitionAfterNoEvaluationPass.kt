package com.instacart.formula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(sideEffectService: SideEffectService) = HasChildFormula(SideEffectFormula(sideEffectService))
}
