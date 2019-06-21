package com.instacart.formula

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

interface ProcessorFormula<Input, State, Effect, RenderModel> : Formula<Input, RenderModel> {

    fun onEffect(input: Input, effect: Effect) = Unit

    fun initialState(input: Input): State

    fun process(
        input: Input,
        state: State,
        context: FormulaContext<State, Effect>
    ): ProcessResult<RenderModel>

    override fun state(input: Input): Observable<RenderModel> {
        return ProcessorFormulaRxRuntime.start(input, this, onEffect = {
            onEffect(input, it)
        })
    }
}


