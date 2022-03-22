package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * To use it within a [Formula]:
 * ```
 * Evaluation(
 *   updates = context.updates {
 *     events(stream) {
 *       transition()
 *     }
 *   }
 * )
 * ```
 *
 * @param Event A type of event that the stream produces.
 */
@Deprecated("Use Action instead.")
typealias Stream<Event> = Action<Event>
