package com.instacart.formula.internal

import com.instacart.formula.Formula

class FormulaManagerFactoryImpl : FormulaManagerFactory {

    override fun <Input, State, Output, RenderModel> createChildManager(
        formula: Formula<Input, State, Output, RenderModel>,
        input: Input,
        transitionLock: TransitionLock
    ): FormulaManager<Input, State, Output, RenderModel> {
        val initial = formula.initialState(input)
        return ProcessorManager(initial, transitionLock, this)
    }
}
