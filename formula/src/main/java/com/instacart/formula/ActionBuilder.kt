package com.instacart.formula

/**
 * Action builder registers [actions][Action] during formula evaluation. Use
 * [FormulaContext.actions] within [Formula.evaluate] and call [onEvent].
 *
 * ```
 * context.actions {
 *   Action.onInit().onEvent {
 *     transition { analytics.trackPageInitialized() }
 *   }
 * }
 * return PageOutput(...)
 * ```
 */
interface ActionBuilder<out Input, State> {
    /**
     * Current input associated with the Formula evaluation.
     */
    val input: Input
    /**
     * Current state associated with the Formula evaluation.
     */
    val state: State

    /**
     * Registers an [Action] during formula evaluation. The action is initialized when first
     * registered and cleaned up when it is no longer registered.
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    fun <Event> events(
        action: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )

    /**
     * Registers an [Action] during formula evaluation. The action is initialized when first
     * registered and cleaned up when it is no longer registered.
     * Example:
     * ```
     * Action.onInit().onEvent {
     *   transition { /* */ }
     * }
     * ```
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    fun <Event> Action<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    )

    /**
     * Registers an [Action] during formula evaluation. The action is initialized when first
     * registered and cleaned up when it is no longer registered.
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
    fun <Event> Action<Event>.onEventWithExecutionType(
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )
}
