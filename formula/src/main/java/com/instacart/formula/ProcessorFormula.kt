package com.instacart.formula

/**
 * TODO: would be good to rename to `Formula` and rename existing `Formula` to `RxFormula`.
 */
interface ProcessorFormula<Input, State, Effect, RenderModel> {

    fun initialState(input: Input): State

    /**
     * This method is called any time [State] changes. Use this method to
     * 1. Create the [RenderModel]
     * 2. Define what [Stream]s should run.
     * 3. Define children Formulas.
     */
    fun process(
        input: Input,
        state: State,
        context: FormulaContext<State, Effect>
    ): ProcessResult<RenderModel>
}
