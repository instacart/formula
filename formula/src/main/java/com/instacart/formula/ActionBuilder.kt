package com.instacart.formula

/**
 * Action builder is used to create a list of deferred [actions][Action] that
 * will be executed by Formula runtime after Formula evaluation finished. To
 * add your actions, use [FormulaContext.actions] within [Formula.evaluate]
 * and call [onEvent].
 *
 * ```
 * Evaluation(
 *   actions = context.actions {
 *     Action.onInit().onEvent {
 *       transition { analytics.trackPageInitialized() }
 *     }
 *   }
 * )
 * ```
 */
abstract class ActionBuilder<out Input, State>(
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
     * Adds an [Action] as part of this [Evaluation]. [Action] will be initialized
     * when it is initially added and cleaned up when it is not returned
     * as part of [Evaluation].
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    abstract fun <Event> events(
        action: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds an [Action] as part of this [Evaluation]. [Action] will be initialized
     * when it is initially added and cleaned up when it is not returned
     * as part of [Evaluation].
     * Example:
     * ```
     * Action.onInit().onEvent {
     *   transition { /* */ }
     * }
     * ```
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    abstract fun <Event> Action<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds an [Action] as part of this [Evaluation]. [Action] will be initialized
     * when it is initially added and cleaned up when it is not returned
     * as part of [Evaluation].
     *
     * Example:
     * ```
     * Action.onInit().onEventWithExecutionType(Transition.Background) {
     *     transition { /* */ }
     * }
     * ```
     *
     * @param executionType Defines the execution model for this event such as threading and timing.
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    abstract fun <Event> Action<Event>.onEventWithExecutionType(
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )
}