package com.instacart.formula.events

/**
 * A deferred transition contains an [event] and a related [transition] which can
 * be executed using the [execute] function. If the formula is ready, it will be
 * executed immediately. Otherwise, it will be added to the queue and executed
 * when formula is ready. If the formula is re-evaluated and [listener] is disabled
 * before the transition is executed, this transition will be ignored.
 */
class DeferredTransition<Input, State, EventT> internal constructor(
    private val listener: ListenerImpl<Input, State, EventT>,
    private val event: EventT,
) {

    fun execute() {
        listener.applyInternal(event)
    }
}