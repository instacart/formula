package com.instacart.formula

import com.instacart.formula.internal.StreamKey

interface FormulaContext<State, Effect> {
    fun transition(state: State)

    fun transition(state: State, effect: Effect?)

    fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        tag: String = "",
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel


    fun streams(init: StreamBuilder<State, Effect>.() -> Unit): List<StreamConnection<*, *>>

    class StreamBuilder<State, Effect>(
        private val transition: (Transition<State, Effect>) -> Unit
    ) {
        internal val streams = mutableListOf<StreamConnection<*, *>>()

        fun <Input : Any, Output> stream(
            stream: Stream<Input, Output>,
            input: Input,
            tag: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            val connection = createConnection(stream, input, tag, onEvent)
            if (streams.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.key}")
            }

            streams.add(connection)
        }

        fun <Output> stream(
            stream: Stream<Unit, Output>,
            tag: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            stream(stream, Unit, tag, onEvent)
        }


        private fun <Input : Any, Output> createConnection(
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
                    transition(value)
                }
            )
        }
    }
}
