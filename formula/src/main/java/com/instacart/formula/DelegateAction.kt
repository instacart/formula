package com.instacart.formula

/**
 * Delegate action enables us to wrap an existing action and expose custom information as
 * part of the type. For example, if you have update email action and want to expose new
 * email information with it.
 * ```
 * class UpdateEmailAction(
 *     // Exposed custom information as part of the action
 *     val newEmail: String,
 *     // Action which will be executed
 *     action: Action<UpdateEmailResult>,
 * ): DelegateAction(action)
 * ```
 */
abstract class DelegateAction<Event>(val action: Action<Event>): Action<Event> by action
