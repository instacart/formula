package com.instacart.formula.internal

import com.instacart.formula.Formula

interface FormulaManagerFactory {

    fun <Input, State, Output, RenderModel> createChildManager(
        formula: Formula<Input, State, Output, RenderModel>,
        input: Input,
        transitionLock: TransitionLock
    ): FormulaManager<Input, State, Output>
}
