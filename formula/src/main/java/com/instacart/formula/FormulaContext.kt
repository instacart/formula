package com.instacart.formula

interface FormulaContext<State, Effect> {
    fun transition(state: State)

    fun transition(state: State, effect: Effect?)

    fun <Input : Any, Output> worker(
        processor: Processor<Input, Output>,
        input: Input,
        tag: String = "",
        onEvent: (Output) -> Transition<State, Effect>
    ): Worker<Input, Output> {
        return Worker(
            key = Worker.Key(input, processor::class, tag),
            input = input,
            processor = processor,
            onEvent = {
                val value = onEvent(it)
                transition(value.state, value.effect)
            }
        )
    }

    fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        tag: String = "",
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel
}
