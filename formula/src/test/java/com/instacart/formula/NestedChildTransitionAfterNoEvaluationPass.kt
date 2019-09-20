package com.instacart.formula

object NestedChildTransitionAfterNoEvaluationPass {
    fun formula(onSideEffect: () -> Unit) =
        HasChildFormula.create(
            HasChildFormula.create(
                SideEffectFormula.create(onSideEffect)
            )
        )
}
