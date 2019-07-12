package com.instacart.formula

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula() = HasChildFormula(HasChildFormula(SideEffectFormula()))
}
