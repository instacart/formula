package com.instacart.formula

object ChildTransitionAfterNoEvaluationPass {
    fun formula() = HasChildFormula(SideEffectFormula())
}
