package com.instacart.formula.internal

import com.instacart.formula.Formula

interface FormulaManagerFactory {

    fun <Input, State, RenderModel> createChildManager(
        formula: Formula<Input, State, RenderModel>,
        input: Input,
        transitionLock: TransitionLock,
        transitionListener: TransitionListener
    ): FormulaManager<Input, RenderModel>
}
