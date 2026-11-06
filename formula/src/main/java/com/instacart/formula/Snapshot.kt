package com.instacart.formula

/**
 * A snapshot is a short-lived class that is used within [Formula.evaluate]. It provides
 * the [input], [state] and [context] that can be used during evaluation. Any time there
 * is a state change within the Formula hierarchy, a new snapshot will be generated and
 * [Formula.evaluate] will be called again.
 */
interface Snapshot<out Input, State> {

    /**
     * The current Formula input value passed by the parent.
     */
    val input: Input

    /**
     * The current Formula state value.
     */
    val state: State

    /**
     * Context is a short-lived object associated with the current evaluation. It should not be
     * used after evaluation has finished.
     */
    val context: FormulaContext<Input, State>

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
     * Example:
     * ```kotlin
     * Action.onInit().onEvent {
     *   transition { /* side effect */ }
     * }
     * ```
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
     * Example:
     * ```kotlin
     * Action.onInit().onEventWithExecutionType(Transition.Background) {
     *   transition { /* side effect */ }
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
