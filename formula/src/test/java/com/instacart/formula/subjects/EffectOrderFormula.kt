package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.FormulaContext
import com.instacart.formula.Listener
import com.instacart.formula.Stream

class EffectOrderFormula : Formula<EffectOrderFormula.Input, EffectOrderFormula.State, EffectOrderFormula.Output>() {

    data class Event(val id: Int)

    data class Input(
        val onEvent: (Event) -> Unit
    )

    data class State(
        val lastEventId: Int = 0,
        val pending: Boolean = false,
    ) {
        fun nextId(): Pair<State, Int> {
            val nextId = lastEventId + 1
            return Pair(copy(lastEventId = nextId), nextId)
        }
    }

    data class Output(
        val triggerEvent: Listener<Unit>
    )

    override fun initialState(input: Input): State = State()

    override fun evaluate(
        input: Input,
        state: State,
        context: FormulaContext<State>
    ): Evaluation<Output> {
        return Evaluation(
            output = Output(
                triggerEvent = context.onEvent {
                    val (updated, eventId) = state.nextId()
                    transition(updated.copy(pending = true)) {
                        input.onEvent(Event(eventId))
                    }
                }
            ),
            updates = context.updates {
                // Formula will run `Stream.onData()` before `input.onEvent` is called above.
                val pending = state.pending
                if (pending) {
                    Stream.onData(pending).onEvent {
                        val (updated, eventId) = state.nextId()
                        transition(updated.copy(pending = false)) {
                            input.onEvent(Event(eventId))
                        }
                    }
                }
            }
        )
    }
}