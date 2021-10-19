package com.instacart.formula.subjects

import com.instacart.formula.Evaluation
import com.instacart.formula.Formula
import com.instacart.formula.Snapshot
import com.instacart.formula.test.TestEventCallback
import com.instacart.formula.test.TestableRuntime

class EventFormula<EventT>(
    runtime: TestableRuntime,
    private val events: List<EventT>,
) : Formula<Unit, Int, Int>() {

    data class Event<EventT>(
        val state: Int,
        val event: EventT
    )

    private val stream = runtime.emitEvents(events)
    private val captured = TestEventCallback<Event<EventT>>()

    fun capturedStates() = captured.values().map { it.state }
    fun capturedEvents() = captured.values().map { it.event }

    override fun initialState(input: Unit): Int = 0

    override fun Snapshot<Unit, Int>.evaluate(): Evaluation<Int> {

        return Evaluation(
            output = state,
            updates = context.updates {
                events(stream) {
                    val newState = state + 1
                    transition(newState) {
                        captured(Event(newState, it))
                    }
                }
            }
        )
    }
}