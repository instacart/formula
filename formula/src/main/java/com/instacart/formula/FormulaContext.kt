package com.instacart.formula

import com.instacart.formula.internal.UpdateKey
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables

interface FormulaContext<State, Effect> {
    fun transition(state: State)

    fun transition(state: State, effect: Effect?)

    fun <ChildInput, ChildState, ChildEffect, ChildRenderModel> child(
        formula: ProcessorFormula<ChildInput, ChildState, ChildEffect, ChildRenderModel>,
        input: ChildInput,
        key: String = "",
        onEffect: (ChildEffect) -> Transition<State, Effect>
    ): ChildRenderModel

    fun updates(init: UpdateBuilder<State, Effect>.() -> Unit): List<StreamConnection<*, *>>

    class UpdateBuilder<State, Effect>(
        private val transition: (Transition<State, Effect>) -> Unit
    ) {
        internal val updates = mutableListOf<StreamConnection<*, *>>()

        private fun <Input : Any, Output> add(connection: StreamConnection<Input, Output>) {
            if (updates.contains(connection)) {
                throw IllegalStateException("duplicate stream with key: ${connection.key}")
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

        fun effect(key: String, action: () -> Unit) {
            val stream = object : Stream<Unit, Unit> {
                override fun subscribe(input: Unit, onEvent: (Unit) -> Unit): Disposable {
                    action()
                    return Disposables.disposed()
                }
            }

            val connection = StreamConnection(
                key = UpdateKey(Unit, stream::class, key),
                input = Unit,
                stream = stream,
                onEvent = {}
            )

            add(connection)
        }

        fun <Input : Any> effect(input: Input, key: String = "", action: (Input) -> Unit) {
            val stream = object : Stream<Input, Unit> {
                override fun subscribe(input: Input, onEvent: (Unit) -> Unit): Disposable {
                    action(input)
                    return Disposables.disposed()
                }
            }

            val connection = StreamConnection(
                key = UpdateKey(input, stream::class, key),
                input = input,
                stream = stream,
                onEvent = {}
            )

            add(connection)
        }

        private fun <Input : Any, Output> createConnection(
            stream: Stream<Input, Output>,
            input: Input,
            key: String = "",
            onEvent: (Output) -> Transition<State, Effect>
        ): StreamConnection<Input, Output> {
            return StreamConnection(
                key = UpdateKey(input, stream::class, key),
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
