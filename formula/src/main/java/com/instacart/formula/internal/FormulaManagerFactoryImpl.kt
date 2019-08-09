package com.instacart.formula.internal

import com.instacart.formula.Formula

class FormulaManagerFactoryImpl : FormulaManagerFactory {

    override fun <Input, State, RenderModel> createChildManager(
        formula: Formula<Input, State, RenderModel>,
        input: Input,
        transitionLock: TransitionLock
    ): FormulaManager<Input, State, RenderModel> {
        val initial = formula.initialState(input)
        return FormulaManagerImpl(initial, ScopedCallbacks(formula), transitionLock, this)
    }
}
