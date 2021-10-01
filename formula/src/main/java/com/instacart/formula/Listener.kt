package com.instacart.formula

/**
 * Formula is an event-driven framework. A listener type describes a function that accepts
 * events of type [Event]. It is equivalent to [(Event) -> Unit] type. Event type defines
 * information that needs to be passed when event happens. You can use [Unit] as event type
 * if there is no information that needs to be passed.
 *
 * &nbsp;
 *
 * To create [Listener] within [Formula.evaluate], you should use [FormulaContext.onEvent]:
 * ```
 * val listener = context.onEvent<MyEvent> {
 *   transition()
 * }
 * ```
 *
 * &nbsp;
 *
 * To create [Listener] outside of [Formula], you can just instantiate this interface:
 * ```
 * val listener = Listener<MyEvent> { }
 * ```
 */
fun interface Listener<Event> : (Event) -> Unit

/**
 * A convenience extension function that allows you do invoke Listener<Unit> without
 * having to pass Unit value yourself.
 */
operator fun Listener<Unit>.invoke() = invoke(Unit)