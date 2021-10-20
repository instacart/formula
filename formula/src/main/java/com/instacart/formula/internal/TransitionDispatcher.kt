package com.instacart.formula.internal

import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext

internal class TransitionDispatcher<out Input, State>(
    override val input: Input,
    override val state: State,
    private val handleTransition: (Transition.Result<State>) -> Unit,
    var transitionId: TransitionId
) : TransitionContext<Input, State> {
    var running = false
    var terminated = false

    private fun dispatch(transition: Transition.Result<State>) {
        if (!running) {
            throw IllegalStateException("Transitions are not allowed during evaluation")
        }

        if (TransitionUtils.isEmpty(transition)) {
            return
        }

        if (!terminated && transitionId.hasTransitioned()) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old event listener: $transition.")
        }

        handleTransition(transition)
    }

    fun <Event> dispatch(
        transition: Transition<Input, State, Event>,
        event: Event
    ) {
        val result = transition.toResult(this, event)
        dispatch(result)
    }
}


internal fun <Input, State, Event> Transition<Input, State, Event>.toResult(
    context: TransitionContext<Input, State>,
    event: Event
): Transition.Result<State> {
    return context.run { toResult(event) }
}