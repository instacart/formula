package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext

class UseInputFormula<Type> : Formula<Type, Type, Type> {
    override fun initialState(input: Type): Type = input

    override fun onInputChanged(oldInput: Type, input: Type, state: Type): Type {
        // We override our state with what parent provides.
        return input
    }

    override fun evaluate(
        input: Type,
        state: Type,
        context: FormulaContext<Type>
    ): Evaluation<Type> {
        return Evaluation(output = state)
    }
}