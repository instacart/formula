package com.instacart.formula

/**
 * Builds actions during formula evaluation. Since [Snapshot] implements [ActionBuilder], action
 * declarations can be extracted into a private helper and called from `evaluate()`:
 *
 * ```kotlin
 * override fun Snapshot<Input, State>.evaluate(): Evaluation<Output> {
 *   trackPageInitialization()
 *   return Evaluation(output = PageOutput(...))
 * }
 *
 * private fun ActionBuilder<Input, State>.trackPageInitialization() {
 *   Action.onInit().onEvent {
 *     transition { analytics.trackPageInitialized() }
 *   }
 * }
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
     * Adds an [Action] during formula evaluation. The runtime initializes it when first added and
     * cleans it up when it is no longer declared.
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    fun <Event> events(
        action: Action<Event>,
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds an [Action] during formula evaluation. The runtime initializes it when first added and
     * cleans it up when it is no longer declared.
     *
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    fun <Event> Action<Event>.onEvent(
        transition: Transition<Input, State, Event>,
    )

    /**
     * Adds an [Action] during formula evaluation. The runtime initializes it when first added and
     * cleans it up when it is no longer declared.
     *
     * @param executionType Defines the execution model for this event such as threading and timing.
     * @param transition A function that is invoked when [Action] emits an [Event].
     */
    fun <Event> Action<Event>.onEventWithExecutionType(
        executionType: Transition.ExecutionType?,
        transition: Transition<Input, State, Event>,
    )
}
