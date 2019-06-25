package com.instacart.formula

import com.instacart.formula.internal.StreamKey

interface FormulaContext<State, Effect> {
    fun transition(state: State)

    fun transition(state: State, effect: Effect?)

    fun <Input : Any, Output> stream(
        stream: Stream<Input, Output>,
        input: Input,
        tag: String = "",
        onEvent: (Output) -> Transition<State, Effect>
    ): StreamConnection<Input, Output> {
        return StreamConnection(
            key = StreamKey(input, stream::class, tag),
            input = input,
            stream = stream,
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


    fun streams(init: StreamBuilder<State, Effect>.() -> Unit): List<StreamConnection<*, *>>

    class StreamBuilder<State, Effect>(
        private val context: FormulaContext<State, Effect>
    ) {
        internal val streams = mutableListOf<StreamConnection<*, *>>()

        fun <Input : Any, Output> stream(
            stream: Stream<Input, Output>,
            input: Input,
            tag: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            streams.add(context.stream(stream, input, tag, onEvent))
        }

        fun <Output> stream(
            stream: Stream<Unit, Output>,
            tag: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            stream(stream, Unit, tag, onEvent)
        }
    }
}
