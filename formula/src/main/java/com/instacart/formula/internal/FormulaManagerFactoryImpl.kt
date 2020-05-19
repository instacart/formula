package com.instacart.formula.internal

import com.instacart.formula.Formula

class FormulaManagerFactoryImpl : FormulaManagerFactory {

    override fun <Input, State, RenderModel> createChildManager(
        formula: Formula<Input, State, RenderModel>,
        input: Input,
        transitionLock: TransitionLock,
        transitionListener: TransitionListener
    ): FormulaManager<Input, RenderModel> {
        return FormulaManagerImpl(formula, input, ScopedCallbacks(formula), transitionLock, this, transitionListener)
    }
}
