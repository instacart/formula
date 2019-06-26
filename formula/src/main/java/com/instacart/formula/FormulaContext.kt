package com.instacart.formula

import io.reactivex.Observable

interface FormulaContext<State, Effect> {
    fun transition(state: State)

    fun transition(state: State, effect: Effect?)

    fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: Formula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: String = "",
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel

    fun updates(init: UpdateBuilder<State, Effect>.() -> Unit): List<Update>


    class UpdateBuilder<State, Effect>(
        private val transition: (Transition<State, Effect>) -> Unit
    ) {
        internal val updates = mutableListOf<Update>()

        private fun add(connection: Update) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.keyAsString()}")
            }

            updates.add(connection)
        }

        fun <Input : Any, Output> events(
            stream: Stream<Input, Output>,
            input: Input,
            key: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            add(createConnection(stream, input, key, onEvent))
        }

        fun <Output> events(
            stream: Stream<Unit, Output>,
            key: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            events(stream, Unit, key, onEvent)
        }

        fun <Output> events(
            key: String,
            observable: Observable<Output>,
            onEvent: (Output) -> Transition<State, Effect>
        ) {
            val stream = object : RxStream<Unit, Output> {
                override fun observable(input: Unit): Observable<Output> {
                    return observable
                }
            }

            events(stream, key, onEvent)
        }

        /**
         * Define a side effect for which the uniqueness is tied only to [key].
         */
        fun effect(key: String, action: () -> Unit) {
            val connection = Update.Effect(
                input = Unit,
                key = key,
                action = action
            )

            add(connection)
        }

        /**
         * Define a side effect for which the uniqueness is tied to [key] and [input].
         */
        fun <Input : Any> effect(key: String, input: Input, action: (Input) -> Unit) {
            val connection = Update.Effect(
                input = input,
                key = key,
                action = {
                    action(input)
                }
            )

            add(connection)
        }

        private fun <Input : Any, Output> createConnection(
            stream: Stream<Input, Output>,
            input: Input,
            key: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ): Update.Stream<Input, Output> {
            return Update.Stream(
                key = Update.Stream.Key(input, stream::class, key),
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
