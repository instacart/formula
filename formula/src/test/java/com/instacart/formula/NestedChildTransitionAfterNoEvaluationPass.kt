package com.instacart.formula

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula(sideEffectService: SideEffectService) =
        HasChildFormula(HasChildFormula(SideEffectFormula(sideEffectService)))
}
