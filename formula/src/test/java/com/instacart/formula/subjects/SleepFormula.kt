package com.instacart.formula.subjects

import com.instacart.formula.Action
import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot

class SleepFormula : Formula<String, SleepFormula.State, SleepFormula.Output>() {

    data class SleepEvent(
        val duration: Long,
        val threadName: String,
    )

    data class State(
        val sleepEvents: List<SleepEvent> = emptyList(),
        val pendingEvent: SleepEvent? = null,
    )

    data class Output(
        val sleepEvents: List<SleepEvent>,
        val onSleep: (Long) -> Unit,
    )

    override fun key(input: String): Any? {
        return input
    }

    override fun initialState(input: String): State {
        return State()
    }

    override fun Snapshot<String, State>.evaluate(): Evaluation<Output> {
        return Evaluation(
            output = Output(
                sleepEvents = state.sleepEvents,
                onSleep = context.onEvent {
                    val newEvent = SleepEvent(
                        duration = it,
                        threadName = Thread.currentThread().name,
                    )
                    transition(state.copy(pendingEvent = newEvent))
                }
            ),
            actions = context.actions {
                state.pendingEvent?.let {
                    Action.onData(it).onEvent { event ->
                        // Using sleep to control multi-threaded events
                        Thread.sleep(event.duration)
                        val events = state.sleepEvents
                        val newState = state.copy(
                            sleepEvents = events + event,
                            pendingEvent = null,
                        )
                        transition(newState)
                    }
                }
            }
        )
    }
}