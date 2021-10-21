package com.instacart.formula

/**
 * Stream builder is used to declare [streams][Stream] within Formula
 * [evaluation][Formula.evaluate]. Call [FormulaContext.updates] to start
 * the process and use [events] or [onEvent] to provide a [Transition]
 * which will be called when stream emits an event.
 */
abstract class StreamBuilder<out Input, State>(
    /**
     * Current input associated with the Formula evaluation.
     */
    val input: Input,
    /**
     * Current state associated with the Formula evaluation.
     */
    val state: State,
) {

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     */
    abstract fun <Event> events(
        stream: Stream<Event>,
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     */
    abstract fun <Event> onEvent(
        stream: Stream<Event>,
        avoidParameterClash: Any = this,
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds a [Stream] as part of this [Evaluation]. [Stream] will be subscribed when it is initially added
     * and unsubscribed when it is not returned as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Stream] produces an [Event].
     *
     * Example:
     * ```
     * Stream.onInit().onEvent {
     *   transition { /* */ }
     * }
     * ```
     */
    abstract fun <Event> Stream<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    )
}