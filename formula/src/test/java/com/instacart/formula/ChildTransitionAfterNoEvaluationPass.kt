package com.instacart.formula

object ChildTransitionAfterNoEvaluationPass {
    fun formula(sideEffectService: SideEffectService) = HasChildFormulaV2(SideEffectFormula(sideEffectService)) {
        Unit
    }
}
